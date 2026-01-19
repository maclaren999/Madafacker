package repository

import com.bbuddies.madafaker.common_domain.auth.FirebaseAuthStatus
import com.bbuddies.madafaker.common_domain.auth.TokenRefreshService
import com.bbuddies.madafaker.common_domain.model.AuthenticationState
import com.bbuddies.madafaker.common_domain.model.User
import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import com.bbuddies.madafaker.common_domain.repository.UserRepository
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import local.MadafakerDao
import remote.api.MadafakerApi
import remote.api.request.CreateUserRequest
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AUTH_REPO"

/**
 * UserRepository implementation with optimistic authentication.
 *
 * Auth Philosophy:
 * - Session is active if user has logged in and not explicitly logged out
 * - Cached user data is trusted for immediate app access
 * - Firebase/network validation happens in background
 * - Only explicit logout or confirmed auth failure clears session
 *
 * IMPORTANT: Firebase Auth may report SignedOut on cold start before finishing initialization.
 * We use cached session data (isSessionActive + cached user) as the source of truth,
 * and validate Firebase in the background.
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val webService: MadafakerApi,
    private val preferenceManager: PreferenceManager,
    private val localDao: MadafakerDao,
    private val tokenRefreshService: TokenRefreshService
) : UserRepository {

    val firebaseMessaging by lazy { FirebaseMessaging.getInstance() }
    private val firebaseCrashlytics: FirebaseCrashlytics by lazy { FirebaseCrashlytics.getInstance() }
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Track if we've attempted background validation this session
    private var hasAttemptedBackgroundValidation = false

    // Track if background validation is currently in progress
    private var isBackgroundValidationInProgress = false

    /**
     * Authentication State Flow - OPTIMISTIC approach:
     * 1. If session is active AND we have cached user → Authenticated immediately
     * 2. If session is NOT active → NotAuthenticated
     * 3. Background: validate Firebase and refresh tokens as needed
     *
     * This flow does NOT depend on Firebase state - that's validated separately.
     */
    override val authenticationState: StateFlow<AuthenticationState> =
        combine(
            preferenceManager.isSessionActive,
            preferenceManager.userId
        ) { sessionActive, userId ->
            Timber.tag(TAG).d("authenticationState combine: sessionActive=$sessionActive, userId=$userId")
            resolveAuthState(sessionActive, userId)
        }
            .stateIn(
                scope = repositoryScope,
                started = SharingStarted.Eagerly,
                initialValue = AuthenticationState.Loading
            )

    init {
        // Start background validation after initial state is determined
        repositoryScope.launch {
            // Small delay to let initial state settle and UI to render
            delay(500)
            performBackgroundValidation()
        }
    }

    /**
     * Resolves authentication state based on local session data.
     * This does NOT depend on Firebase state - Firebase validation is background.
     */
    private suspend fun resolveAuthState(sessionActive: Boolean, userId: String?): AuthenticationState {
        Timber.tag(TAG).d("resolveAuthState: sessionActive=$sessionActive, userId=$userId")

        if (!sessionActive) {
            Timber.tag(TAG).d("Session not active -> NotAuthenticated")
            return AuthenticationState.NotAuthenticated
        }

        if (userId == null) {
            Timber.tag(TAG).w("Session active but no userId -> NotAuthenticated (inconsistent state)")
            // Inconsistent state - session marked active but no user ID
            // This shouldn't happen, but handle gracefully
            return AuthenticationState.NotAuthenticated
        }

        // Try to get cached user
        val cachedUser = try {
            localDao.getUserById(userId)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to get cached user by ID: $userId")
            null
        }

        return if (cachedUser != null) {
            Timber.tag(TAG).d("Found cached user: ${cachedUser.name} (id=${cachedUser.id})")
            updateCrashlyticsUserIdentification(cachedUser)
            AuthenticationState.Authenticated(cachedUser)
        } else {
            // Try any cached user as fallback
            val anyUser = try {
                localDao.getAnyUser()
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Failed to get any cached user")
                null
            }

            if (anyUser != null) {
                Timber.tag(TAG).d("Using fallback cached user: ${anyUser.name}")
                updateCrashlyticsUserIdentification(anyUser)
                AuthenticationState.Authenticated(anyUser)
            } else {
                Timber.tag(TAG).w("Session active but no cached user found -> attempting network fetch")
                // Last resort: try network
                fetchUserFromNetwork()
            }
        }
    }

    /**
     * Background validation: Wait for Firebase to initialize, then refresh token if needed.
     * This runs AFTER the user already has access to the app.
     */
    private suspend fun performBackgroundValidation() {
        if (hasAttemptedBackgroundValidation) {
            Timber.tag(TAG).d("Background validation already attempted this session")
            return
        }
        if (isBackgroundValidationInProgress) {
            Timber.tag(TAG).d("Background validation already in progress")
            return
        }

        hasAttemptedBackgroundValidation = true
        isBackgroundValidationInProgress = true

        val sessionActive = preferenceManager.isSessionActive.value
        if (!sessionActive) {
            Timber.tag(TAG).d("No active session, skipping background validation")
            isBackgroundValidationInProgress = false
            return
        }

        Timber.tag(TAG).d("=== Starting background validation ===")

        try {
            // CRITICAL: Wait for Firebase to finish initializing before making decisions
            Timber.tag(TAG).d("Waiting for Firebase to initialize...")
            val firebaseStatus = tokenRefreshService.awaitInitialization(timeoutMs = 5000)
            Timber.tag(TAG).d("Firebase initialization complete: $firebaseStatus")

            when (firebaseStatus) {
                FirebaseAuthStatus.SignedIn -> {
                    // Firebase has user - refresh token
                    Timber.tag(TAG).d("Firebase confirmed SignedIn - refreshing token")
                    refreshTokenAndSyncUser()
                }

                FirebaseAuthStatus.SignedOut -> {
                    // Firebase has no user after initialization - this is a real issue
                    Timber.tag(TAG).w("Firebase confirmed SignedOut - attempting recovery")
                    attemptFirebaseRecovery()
                }

                FirebaseAuthStatus.Initializing -> {
                    // Still initializing after timeout - unusual, log and skip
                    Timber.tag(TAG).w("Firebase still initializing after timeout - skipping validation")
                }
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Background validation failed")
        } finally {
            isBackgroundValidationInProgress = false
            Timber.tag(TAG).d("=== Background validation complete ===")
        }
    }

    /**
     * Refresh token and sync user data from server.
     */
    private suspend fun refreshTokenAndSyncUser() {
        try {
            val newToken = tokenRefreshService.refreshFirebaseIdToken(forceRefresh = false)
            preferenceManager.updateFirebaseIdToken(newToken)
            Timber.tag(TAG).d("Token refresh successful (length=${newToken.length})")

            // Optionally sync user data from server
            tryRefreshUserFromServer()
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Token refresh failed (non-critical)")
            // Don't clear session - token will be refreshed on next API call if needed
        }
    }

    /**
     * Attempt to recover Firebase session when it reports SignedOut.
     *
     * This can happen when:
     * 1. Firebase truly has no session (user logged out or never logged in)
     * 2. Firebase session expired (very rare, Firebase handles refresh internally)
     * 3. Firebase data was cleared
     *
     * We try to restore using stored Google ID token, but this will fail
     * if the token is expired (>1 hour old).
     */
    private suspend fun attemptFirebaseRecovery() {
        val storedGoogleToken = preferenceManager.googleIdAuthToken.value
        val storedFirebaseToken = preferenceManager.firebaseIdToken.value
        val storedFirebaseUid = preferenceManager.firebaseUid.value
        val storedUserId = preferenceManager.userId.value

        Timber.tag(TAG).d("=== Attempting Firebase recovery ===")
        Timber.tag(TAG).d("Has stored Google token: ${storedGoogleToken != null}")
        Timber.tag(TAG).d("Has stored Firebase token: ${storedFirebaseToken != null}")
        Timber.tag(TAG).d("Stored Firebase UID: $storedFirebaseUid")
        Timber.tag(TAG).d("Stored User ID: $storedUserId")

        // Log token age if possible (Google ID tokens have exp claim)
        if (storedGoogleToken != null) {
            try {
                val parts = storedGoogleToken.split(".")
                if (parts.size == 3) {
                    val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE))
                    Timber.tag(TAG).d("Google token payload (partial): ${payload.take(100)}...")
                }
            } catch (e: Exception) {
                Timber.tag(TAG).d("Could not decode token: ${e.message}")
            }
        }

        if (storedGoogleToken == null) {
            Timber.tag(TAG).e("No stored Google token - cannot recover Firebase session")
            // We have a session but no way to restore Firebase
            // Let the AuthInterceptor handle token refresh on API calls
            // If API calls fail, user will be logged out
            return
        }

        // Try to restore Firebase session with stored Google token
        Timber.tag(TAG).d("Attempting to restore Firebase session with stored Google token...")
        val restorationSuccessful = tokenRefreshService.restoreFirebaseSession(storedGoogleToken)

        if (restorationSuccessful) {
            Timber.tag(TAG).d("Firebase session restored successfully!")
            // Refresh the Firebase ID token now
            refreshTokenAndSyncUser()
        } else {
            Timber.tag(TAG).e("Firebase session restoration failed - Google token likely expired")
            // Google ID token has expired (>1 hour old)
            // The stored Firebase ID token might still work for API calls if not expired
            // Let the AuthInterceptor handle this - if API calls return 401, force logout

            if (storedFirebaseToken != null) {
                Timber.tag(TAG).d("Will use cached Firebase token for API calls")
                // Validate the cached token by making an API call
                try {
                    val user = webService.getCurrentUser()
                    Timber.tag(TAG).d("Cached Firebase token still valid - user: ${user.name}")
                    cacheUser(user)
                } catch (e: HttpException) {
                    if (e.code() == 401) {
                        Timber.tag(TAG).e("Cached Firebase token is also expired - forcing logout")
                        forceLogout("Firebase session lost and cached token expired")
                    } else {
                        Timber.tag(TAG).w(e, "API error during validation (non-401)")
                    }
                } catch (e: Exception) {
                    Timber.tag(TAG).w(e, "Network error during validation - will retry later")
                }
            } else {
                Timber.tag(TAG).e("No cached Firebase token - forcing logout")
                forceLogout("Firebase session lost and no cached token")
            }
        }
    }

    /**
     * Try to refresh user data from server (non-critical).
     */
    private suspend fun tryRefreshUserFromServer() {
        try {
            val networkUser = webService.getCurrentUser()
            cacheUser(networkUser)
            Timber.tag(TAG).d("User data synced from server: ${networkUser.name}")
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Failed to sync user from server (non-critical)")
            // Don't log out on network failure - cached data is sufficient
        }
    }

    /**
     * Fetch user from network when no cache exists.
     */
    private suspend fun fetchUserFromNetwork(): AuthenticationState {
        Timber.tag(TAG).d("Attempting to fetch user from network...")

        // First try to refresh token if Firebase is available
        try {
            if (tokenRefreshService.hasFirebaseUser()) {
                val freshToken = tokenRefreshService.refreshFirebaseIdToken(forceRefresh = true)
                preferenceManager.updateFirebaseIdToken(freshToken)
            }
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Token refresh before network fetch failed")
        }

        return try {
            val networkUser = webService.getCurrentUser()
            cacheUser(networkUser)
            updateCrashlyticsUserIdentification(networkUser)
            Timber.tag(TAG).d("Fetched user from network: ${networkUser.name}")
            AuthenticationState.Authenticated(networkUser)
        } catch (e: HttpException) {
            if (e.code() == 401) {
                Timber.tag(TAG).e("401 during initial user fetch - invalid session")
                // 401 means our session is definitely invalid
                forceLogout("Received 401 during initial fetch")
                AuthenticationState.NotAuthenticated
            } else {
                Timber.tag(TAG).e(e, "HTTP error fetching user: ${e.code()}")
                AuthenticationState.Error(e)
            }
        } catch (e: IOException) {
            Timber.tag(TAG).w(e, "Network error fetching user - staying authenticated offline")
            // Network error - stay authenticated (user might be offline)
            AuthenticationState.Error(e)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Unknown error fetching user")
            AuthenticationState.Error(e)
        }
    }

    /**
     * Force logout - clears all auth state and signs out from Firebase.
     * Only call this for confirmed auth failures, NOT for transient issues.
     */
    private suspend fun forceLogout(reason: String) {
        Timber.tag(TAG).w("=== FORCE LOGOUT ===")
        Timber.tag(TAG).w("Reason: $reason")
        try {
            tokenRefreshService.signOut()
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error during Firebase signOut")
        }
        clearAllUserData()
        Timber.tag(TAG).w("=== FORCE LOGOUT COMPLETE ===")
    }

    override val currentUser: StateFlow<User?> = authenticationState
        .map { state ->
            when (state) {
                is AuthenticationState.Authenticated -> state.user
                is AuthenticationState.NotAuthenticated -> null
                is AuthenticationState.Error -> null
                is AuthenticationState.Loading -> return@map null // Don't emit during loading
            }
        }
        .distinctUntilChanged() // Only emit when user actually changes
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly, // Start immediately to avoid initial null
            initialValue = null
        )

    // Add a suspend function that waits for authentication to complete
    override suspend fun awaitCurrentUser(): User? {
        return authenticationState
            .filterNot { it is AuthenticationState.Loading } // Skip loading states
            .map { state ->
                when (state) {
                    is AuthenticationState.Authenticated -> state.user
                    else -> null
                }
            }
            .first() // Get the first non-loading result
    }

    override val isUserLoggedIn: StateFlow<Boolean> = authenticationState
        .map { it is AuthenticationState.Authenticated }
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    override suspend fun getCurrentUserOrThrow(): User =
        when (val state = authenticationState.value) {
            is AuthenticationState.Authenticated -> state.user
            is AuthenticationState.NotAuthenticated -> throw IllegalStateException("User not authenticated")
            is AuthenticationState.Loading -> throw IllegalStateException("Authentication state is loading")
            is AuthenticationState.Error -> throw state.exception
        }

    override suspend fun getCurrentUser(forceRefresh: Boolean): User? = withContext(Dispatchers.IO) {
        if (forceRefresh) {
            // Force refresh: fetch fresh data from server
            try {
                val freshUser = webService.getCurrentUser()
                cacheUser(freshUser)
                Timber.d("Force refreshed user data from server")
                return@withContext freshUser
            } catch (e: Exception) {
                Timber.e(e, "Failed to force refresh user data, falling back to cache")
                // Fall through to cache lookup on error
            }
        }

        // Cache-only lookup - network fetching is handled by authenticationState flow
        preferenceManager.userId.value?.let { userId ->
            try {
                localDao.getUserById(userId)
            } catch (exception: Exception) {
                Timber.e(exception, "Error getting user from cache")
                null
            }
        }
    }

    override suspend fun updateUserName(name: String): User = withContext(Dispatchers.IO) {
        val updatedUser = webService.updateCurrentUser(name)
        // Update local cache
        cacheUser(updatedUser)
        updatedUser
    }

    override suspend fun isNameAvailable(name: String): Boolean = withContext(Dispatchers.IO) {
        webService.checkNameAvailability(name).nameIsAvailable
    }

    override suspend fun clearAllUserData() = withContext(Dispatchers.IO) {
        Timber.tag(TAG).d("clearAllUserData() called - clearing all auth data")
        try {
            // Delete current FCM token to ensure fresh token for new account
            firebaseMessaging.deleteToken().await()
            Timber.tag(TAG).d("FCM token deleted")
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Failed to delete FCM token (non-critical)")
        }

        // Clear Crashlytics user identification
        clearCrashlyticsUserIdentification()

        // Clear all local data
        localDao.clearAllData()
        Timber.tag(TAG).d("Local DB cleared")

        // Clear preferences (this also clears sessionActive)
        preferenceManager.clearUserData()
        Timber.tag(TAG).d("Preferences cleared - user is now logged out")
    }

    private suspend fun refreshFcmToken(): String = withContext(Dispatchers.IO) {
        try {
            // Delete current token
            firebaseMessaging.deleteToken().await()
            // Get new token
            firebaseMessaging.token.await()
        } catch (e: Exception) {
            // Fallback to just getting current token if deletion fails
            firebaseMessaging.token.await()
        }
    }

    private suspend fun cacheUser(user: User) {
        localDao.insertUser(user)
        preferenceManager.updateUserId(user.id)
    }

    override suspend fun storeGoogleAuth(
        googleIdToken: String,
        googleUserId: String,
        firebaseIdToken: String,
        firebaseUid: String
    ) = withContext(Dispatchers.IO) {
        Timber.tag(TAG).d("Storing Google auth credentials (firebaseUid=$firebaseUid)")
        preferenceManager.updateAllAuthTokens(googleIdToken, googleUserId, firebaseIdToken, firebaseUid)
        // Mark session as active - this is what keeps user logged in across cold starts
        preferenceManager.setSessionActive(true)
        Timber.tag(TAG).d("Session marked as active")
    }

    override suspend fun refreshFirebaseIdToken(): String = withContext(Dispatchers.IO) {
        try {
            // Get fresh token from Firebase
            val newToken = tokenRefreshService.refreshFirebaseIdToken()

            // Update stored token
            preferenceManager.updateFirebaseIdToken(newToken)

            Timber.tag(TAG).d("Firebase ID token refreshed and updated in preferences")
            newToken
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to refresh Firebase ID token")
            throw e
        }
    }



    override suspend fun createUserWithGoogle(nickname: String, idToken: String, googleUserId: String): User =
        withContext(Dispatchers.IO) {
            try {
                val fcmToken = getFreshFcmToken()
                attemptUserCreation(nickname, fcmToken, idToken)
            } catch (e: Exception) {
                Timber.e(e, "Failed to create user for googleUserId: $googleUserId")
                throw e
            }
        }

    /**
     * Attempts to create a user with the given parameters, handling FCM token conflicts.
     * @param nickname User's chosen nickname
     * @param fcmToken FCM token for notifications
     * @param idToken Google ID token for authentication
     * @return Created User object
     */
    private suspend fun attemptUserCreation(nickname: String, fcmToken: String, idToken: String): User {
        return try {
            val user = webService.createUser(CreateUserRequest(nickname, fcmToken))
            // Note: We only update googleIdToken here since all tokens were already stored in storeGoogleAuth
            preferenceManager.updateAuthToken(idToken)
            cacheUser(user)
            // Ensure session is active after successful creation
            preferenceManager.setSessionActive(true)
            Timber.tag(TAG).d("User created successfully: ${user.name}")
            user
        } catch (e: Exception) {
            if (isDuplicateRegistrationTokenError(e)) {
                Timber.tag(TAG).w("FCM token conflict detected, retrying with fresh token")
                retryUserCreationWithFreshToken(nickname, idToken)
            } else {
                throw e
            }
        }
    }

    /**
     * Retries user creation with a fresh FCM token.
     * @param nickname User's chosen nickname
     * @param idToken Google ID token for authentication
     * @return Created User object
     */
    private suspend fun retryUserCreationWithFreshToken(nickname: String, idToken: String): User {
        val freshFcmToken = refreshFcmToken()
        val user = webService.createUser(CreateUserRequest(nickname, freshFcmToken))
        preferenceManager.updateAuthToken(idToken)
        cacheUser(user)
        preferenceManager.setSessionActive(true)
        Timber.tag(TAG).d("User created on retry: ${user.name}")
        return user
    }

    /**
     * Gets a fresh FCM token, attempting to refresh if needed.
     * @return FCM token string
     */
    private suspend fun getFreshFcmToken(): String {
        return try {
            firebaseMessaging.token.await()
        } catch (e: Exception) {
            Timber.w(e, "Failed to get initial FCM token, attempting refresh")
            refreshFcmToken()
        }
    }

    override suspend fun authenticateWithGoogle(googleIdToken: String, googleUserId: String): User =
        withContext(Dispatchers.IO) {
            try {
                Timber.tag(TAG).d("Authenticating with Google (googleUserId=$googleUserId)")
                // For existing users, get current user info using Firebase ID token in header
                val user = webService.getCurrentUser()

                // Update local storage
                preferenceManager.updateAuthToken(googleIdToken)
                cacheUser(user)
                // Ensure session is active
                preferenceManager.setSessionActive(true)
                Timber.tag(TAG).d("Authenticated successfully: ${user.name}")
                user
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Authentication failed for googleUserId: $googleUserId")
                throw e
            }
        }

    /**
     * Safely updates Firebase Crashlytics with user identification information.
     * This method is wrapped in try-catch to prevent any Crashlytics issues from affecting core functionality.
     *
     * @param user The user object containing ID and FCM token information
     */
    private fun updateCrashlyticsUserIdentification(user: User) {
        try {
            // Set user ID for crash reports
            firebaseCrashlytics.setUserId(user.id)

            // Set FCM token as custom key if available
            user.registrationToken?.let { fcmToken ->
                firebaseCrashlytics.setCustomKey("FCM_token", fcmToken)
            }

            // Set additional user context
            firebaseCrashlytics.setCustomKey("user_name", user.name)
            firebaseCrashlytics.setCustomKey("user_created_at", user.createdAt)

            Timber.d("Updated Crashlytics user identification for user: ${user.id}")
        } catch (e: Exception) {
            // Log the error but don't let it affect the main functionality
            Timber.w(e, "Failed to update Crashlytics user identification for user: ${user.id}")
        }
    }

    /**
     * Safely clears Firebase Crashlytics user identification.
     * Called when user logs out to ensure crash reports are not associated with the previous user.
     */
    private fun clearCrashlyticsUserIdentification() {
        try {
            firebaseCrashlytics.setUserId("anonymous")
            firebaseCrashlytics.setCustomKey("FCM_token", "")
            firebaseCrashlytics.setCustomKey("user_name", "")
            firebaseCrashlytics.setCustomKey("user_coins", 0)
            firebaseCrashlytics.setCustomKey("user_created_at", "")

            Timber.d("Cleared Crashlytics user identification")
        } catch (e: Exception) {
            // Log the error but don't let it affect the logout process
            Timber.w(e, "Failed to clear Crashlytics user identification")
        }
    }

}

private fun isDuplicateRegistrationTokenError(exception: Exception): Boolean {
    return when (exception) {
        is HttpException -> {
            if (exception.code() != 400) return false

            try {
                val errorBody = exception.response()?.errorBody()?.string()
                if (errorBody != null) {
                    // Parse JSON response to check for duplicate registration token error
                    val isRegistrationTokenError = errorBody.contains("registrationToken") &&
                            errorBody.contains("already exists")
                    val isDuplicatedValueError = errorBody.contains("Duplicated value is not allowed")

                    return isRegistrationTokenError || isDuplicatedValueError
                }
                false
            } catch (e: Exception) {
                false
            }
        }

        else -> {
            val message = exception.message?.lowercase() ?: ""
            message.contains("registrationtoken") &&
                    (message.contains("already exists") || message.contains("duplicate"))
        }
    }


}
