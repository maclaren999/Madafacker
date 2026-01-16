package repository

import com.bbuddies.madafaker.common_domain.auth.AuthSessionState
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
import java.io.IOException
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val webService: MadafakerApi,
    private val preferenceManager: PreferenceManager,
    private val localDao: MadafakerDao, // Add this for local caching
    private val tokenRefreshService: TokenRefreshService
) : UserRepository {

    val firebaseMessaging by lazy { FirebaseMessaging.getInstance() }
    private val firebaseCrashlytics: FirebaseCrashlytics by lazy { FirebaseCrashlytics.getInstance() }
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        repositoryScope.launch {
            tokenRefreshService.authState.collect { authState ->
                if (authState is AuthSessionState.SignedIn) {
                    refreshFirebaseTokenIfNeeded()
                }
            }
        }
    }

    override val authenticationState: StateFlow<AuthenticationState> =
        combine(
            tokenRefreshService.authState,
            preferenceManager.userId
        ) { authState, userId ->
            when (authState) {
                AuthSessionState.Initializing -> AuthenticationState.Loading
                AuthSessionState.SignedOut -> AuthenticationState.NotAuthenticated
                AuthSessionState.SignedIn -> resolveAuthenticatedUser(userId)
            }
        }
            .stateIn(
                scope = repositoryScope,
                started = SharingStarted.Eagerly,
                initialValue = AuthenticationState.Loading
            )

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
                localDao.insertUser(freshUser)
                preferenceManager.updateUserId(freshUser.id)
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
        localDao.insertUser(updatedUser) // Direct use of domain model
        preferenceManager.updateUserId(updatedUser.id)
        updatedUser
    }

//    override suspend fun createUser(name: String): User = withContext(Dispatchers.IO) {
//        // First attempt with current FCM token
//        val initialFcmToken = firebaseMessaging.token.await()
//
//        try {
//            val user = webService.createUser(CreateUserRequest(name, initialFcmToken))
//            preferenceManager.updateAuthToken(user.id)
//            localDao.insertUser(user)
//            Timber.tag("USER_REPO").d("User created with FCM token: $initialFcmToken")
//            return@withContext user
//        } catch (e: Exception) {
//            // Check if it's a duplicate registration token error
//            if (isDuplicateRegistrationTokenError(e)) {
//                // Refresh FCM token and retry
//                val freshFcmToken = refreshFcmToken()
//                try {
//                    val user = webService.createUser(CreateUserRequest(name, freshFcmToken))
//                    preferenceManager.updateAuthToken(user.id)
//                    localDao.insertUser(user)
//                    return@withContext user
//                } catch (retryException: Exception) {
//                    // If retry also fails, throw the retry exception
//                    throw retryException
//                }
//            } else {
//                // If it's not a duplicate token error, rethrow original exception
//                throw e
//            }
//        }
//    }

    override suspend fun isNameAvailable(name: String): Boolean = withContext(Dispatchers.IO) {
        webService.checkNameAvailability(name).nameIsAvailable
    }

    override suspend fun clearAllUserData() = withContext(Dispatchers.IO) {
        try {
            // Delete current FCM token to ensure fresh token for new account
            firebaseMessaging.deleteToken().await()
        } catch (e: Exception) {
            // Log but don't fail the logout process if token deletion fails
            // The user should still be able to logout even if FCM cleanup fails
        }

        // Clear Crashlytics user identification
        clearCrashlyticsUserIdentification()

        // Clear all local data
        localDao.clearAllData()
        // Clear preferences
        preferenceManager.clearUserData()
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

    override suspend fun storeGoogleAuth(
        googleIdToken: String,
        googleUserId: String,
        firebaseIdToken: String,
        firebaseUid: String
    ) = withContext(Dispatchers.IO) {
        preferenceManager.updateAllAuthTokens(googleIdToken, googleUserId, firebaseIdToken, firebaseUid)
        Timber.tag("USER_REPO")
            .d("Auth googleIdToken: $googleIdToken \n googleUserId: $googleUserId \n firebaseIdToken: $firebaseIdToken \n firebaseUid: $firebaseUid")
    }

    override suspend fun refreshFirebaseIdToken(): String = withContext(Dispatchers.IO) {
        try {
            // Get fresh token from Firebase
            val newToken = tokenRefreshService.refreshFirebaseIdToken()

            // Update stored token
            preferenceManager.updateFirebaseIdToken(newToken)

            Timber.tag("USER_REPO").d("Firebase ID token refreshed and updated in preferences")
            newToken
        } catch (e: Exception) {
            Timber.e(e, "Failed to refresh Firebase ID token")
            throw e
        }
    }

    private suspend fun refreshFirebaseTokenIfNeeded() {
        try {
            val freshToken = tokenRefreshService.refreshFirebaseIdToken(forceRefresh = true)
            preferenceManager.updateFirebaseIdToken(freshToken)
            Timber.d("Firebase ID token refreshed on auth state update")
        } catch (e: Exception) {
            Timber.w(e, "Failed to refresh Firebase ID token on auth state update")
        }
    }

    private suspend fun resolveAuthenticatedUser(userId: String?): AuthenticationState {
        val cachedUser = userId?.let { localDao.getUserById(it) }
        if (cachedUser != null) {
            updateCrashlyticsUserIdentification(cachedUser)
            return AuthenticationState.Authenticated(cachedUser)
        }

        refreshFirebaseTokenIfNeeded()

        return try {
            val networkUser = webService.getCurrentUser()
            localDao.insertUser(networkUser)
            preferenceManager.updateUserId(networkUser.id)
            updateCrashlyticsUserIdentification(networkUser)
            AuthenticationState.Authenticated(networkUser)
        } catch (e: Exception) {
            val fallbackUser = when {
                e is HttpException && e.code() == 401 -> {
                    Timber.e(e, "Unauthorized while fetching current user, using cached user if available")
                    userId?.let { localDao.getUserById(it) } ?: localDao.getAnyUser()
                }

                e is IOException -> localDao.getAnyUser()

                else -> null
            }

            if (fallbackUser != null) {
                updateCrashlyticsUserIdentification(fallbackUser)
                AuthenticationState.Authenticated(fallbackUser)
            } else {
                AuthenticationState.Error(e)
            }
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
            preferenceManager.updateUserId(user.id)
            localDao.insertUser(user)

            user
        } catch (e: Exception) {
            if (isDuplicateRegistrationTokenError(e)) {
                Timber.w("FCM token conflict detected, retrying with fresh token")
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
        preferenceManager.updateUserId(user.id)
        localDao.insertUser(user)
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
                // For existing users, get current user info using Firebase ID token in header
                val user = webService.getCurrentUser()

                // Update local storage
                preferenceManager.updateAuthToken(googleIdToken)
                preferenceManager.updateUserId(user.id)
                localDao.insertUser(user)

                user
            } catch (e: Exception) {
                Timber.e(e, "Authentication failed for googleUserId: $googleUserId")
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
