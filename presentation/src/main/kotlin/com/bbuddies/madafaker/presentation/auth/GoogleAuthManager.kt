package com.bbuddies.madafaker.presentation.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import com.bbuddies.madafaker.common_domain.auth.FirebaseAuthStatus
import com.bbuddies.madafaker.common_domain.auth.TokenRefreshService
import com.bbuddies.madafaker.presentation.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AUTH_MANAGER"

/**
 * Manages Google authentication operations including credential management,
 * token extraction, and authentication flow.
 *
 * IMPORTANT NOTE ON FIREBASE PERSISTENCE:
 * Firebase Auth SHOULD automatically persist user sessions across app restarts.
 * However, there's a race condition on cold start where `currentUser` may be null
 * until Firebase finishes its async initialization (typically <1 second, but can be longer).
 *
 * Always use [awaitInitialization] before making critical auth decisions on cold start.
 */
@Singleton
class GoogleAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) : TokenRefreshService {
    private val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
    private val credentialManager = CredentialManager.create(context)
    private val firebaseAuth = FirebaseAuth.getInstance()

    // Store Google credentials for later use in account creation
    private var storedGoogleIdToken: String? = null
    private var storedGoogleUserId: String? = null

    private val _firebaseStatus = MutableStateFlow<FirebaseAuthStatus>(FirebaseAuthStatus.Initializing)
    override val firebaseStatus: StateFlow<FirebaseAuthStatus> = _firebaseStatus.asStateFlow()

    // Track if AuthStateListener has been called at least once
    private val _hasReceivedAuthCallback = MutableStateFlow(false)

    // Track the timestamp of first auth callback for debugging
    private var firstAuthCallbackTimestamp: Long = 0

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        val user = auth.currentUser
        val newStatus = if (user != null) {
            FirebaseAuthStatus.SignedIn
        } else {
            FirebaseAuthStatus.SignedOut
        }
        val timestamp = System.currentTimeMillis()
        val previousStatus = _firebaseStatus.value

        // Track first callback
        if (!_hasReceivedAuthCallback.value) {
            firstAuthCallbackTimestamp = timestamp
            _hasReceivedAuthCallback.value = true
        }

        Timber.tag(TAG).d("=== Firebase AuthStateListener TRIGGERED ===")
        Timber.tag(TAG).d("Timestamp: $timestamp")
        Timber.tag(TAG).d("Time since app start: ${timestamp - appStartTimestamp}ms")
        Timber.tag(TAG).d("Previous status: $previousStatus")
        Timber.tag(TAG).d("New status: $newStatus")
        Timber.tag(TAG).d("User UID: ${user?.uid}")
        Timber.tag(TAG).d("User email: ${user?.email}")
        Timber.tag(TAG).d("User lastSignIn: ${user?.metadata?.lastSignInTimestamp}")
        Timber.tag(TAG).d("Has received callback before: ${_hasReceivedAuthCallback.value}")
        Timber.tag(TAG).d("============================================")

        _firebaseStatus.value = newStatus
    }

    private val appStartTimestamp = System.currentTimeMillis()

    init {
        // Log detailed Firebase state on initialization
        val currentUser = firebaseAuth.currentUser
        Timber.tag(TAG).d("=== GoogleAuthManager INIT ===")
        Timber.tag(TAG).d("App start timestamp: $appStartTimestamp")
        Timber.tag(TAG).d("Firebase currentUser (sync check): ${currentUser?.uid}")
        Timber.tag(TAG).d("Firebase currentUser email: ${currentUser?.email}")
        Timber.tag(TAG).d("Firebase currentUser providerId: ${currentUser?.providerId}")
        Timber.tag(TAG).d("Firebase currentUser providerData: ${currentUser?.providerData?.map { it.providerId }}")
        Timber.tag(TAG).d("Firebase currentUser isAnonymous: ${currentUser?.isAnonymous}")
        Timber.tag(TAG).d("Firebase currentUser metadata - creationTime: ${currentUser?.metadata?.creationTimestamp}")
        Timber.tag(TAG)
            .d("Firebase currentUser metadata - lastSignInTime: ${currentUser?.metadata?.lastSignInTimestamp}")

        // If we already have a user synchronously, update status immediately
        if (currentUser != null) {
            Timber.tag(TAG).d("Firebase user available synchronously - marking as SignedIn")
            _firebaseStatus.value = FirebaseAuthStatus.SignedIn
            _hasReceivedAuthCallback.value = true
        }

        Timber.tag(TAG).d("==============================")

        firebaseAuth.addAuthStateListener(authStateListener)
    }

    /**
     * Waits for Firebase to complete its initial auth state restoration.
     *
     * On cold start, Firebase may report SignedOut before finishing initialization.
     * This method waits for the AuthStateListener to be called at least once,
     * then optionally waits a bit longer to catch late sign-in events.
     *
     * @param timeoutMs Maximum time to wait for initialization
     * @return The confirmed FirebaseAuthStatus after initialization
     */
    override suspend fun awaitInitialization(timeoutMs: Long): FirebaseAuthStatus {
        Timber.tag(TAG).d("awaitInitialization(timeout=${timeoutMs}ms) - current status: ${_firebaseStatus.value}")

        // If we already have SignedIn status, return immediately
        if (_firebaseStatus.value == FirebaseAuthStatus.SignedIn) {
            Timber.tag(TAG).d("Already signed in, returning immediately")
            return FirebaseAuthStatus.SignedIn
        }

        // Wait for first AuthStateListener callback
        val result = withTimeoutOrNull(timeoutMs) {
            // First, wait for at least one auth callback
            if (!_hasReceivedAuthCallback.value) {
                Timber.tag(TAG).d("Waiting for first auth callback...")
                _hasReceivedAuthCallback.first { it }
            }

            // If signed in after callback, great!
            if (_firebaseStatus.value == FirebaseAuthStatus.SignedIn) {
                Timber.tag(TAG).d("Got SignedIn after first callback")
                return@withTimeoutOrNull FirebaseAuthStatus.SignedIn
            }

            // Firebase reported SignedOut - but on cold start this might be premature
            // Wait a short grace period to see if Firebase recovers the session
            Timber.tag(TAG).d("Got SignedOut after first callback, waiting grace period...")
            delay(1500) // 1.5 second grace period

            // Check again
            val finalStatus = _firebaseStatus.value
            Timber.tag(TAG).d("After grace period: $finalStatus")
            finalStatus
        }

        val finalStatus = result ?: run {
            Timber.tag(TAG).w("awaitInitialization timed out, using current status")
            _firebaseStatus.value
        }

        Timber.tag(TAG).d("awaitInitialization completed: $finalStatus")
        return finalStatus
    }

    /**
     * Attempts to restore Firebase session using stored Google ID token.
     * Call this when Firebase has no user but we have stored credentials.
     * @param googleIdToken The stored Google ID token
     * @return true if restoration successful, false otherwise
     */
    override suspend fun restoreFirebaseSession(googleIdToken: String): Boolean {
        Timber.tag(TAG).d("Attempting to restore Firebase session...")
        return try {
            val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user

            if (user != null) {
                Timber.tag(TAG).d("Firebase session restored successfully: uid=${user.uid}, email=${user.email}")
                true
            } else {
                Timber.tag(TAG).e("Firebase session restoration returned no user")
                false
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to restore Firebase session: ${e.message}")
            false
        }
    }

    override fun hasFirebaseUser(): Boolean {
        val hasUser = firebaseAuth.currentUser != null
        Timber.tag(TAG).d("hasFirebaseUser: $hasUser (uid=${firebaseAuth.currentUser?.uid})")
        return hasUser
    }

    /**
     * Performs Google authentication and returns the credential response.
     * @param activityContext The Activity context required for credential UI
     * @return GetCredentialResponse if successful, null otherwise
     * @throws GetCredentialException if authentication fails
     */
    suspend fun performGoogleAuthentication(activityContext: Context): GetCredentialResponse? {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            credentialManager.getCredential(
                request = request,
                context = activityContext
            )
        } catch (e: GetCredentialException) {
            Timber.e(e, "Google authentication failed - ${e.localizedMessage}")
            throw e
        }
    }

    /**
     * Extracts and stores Google credentials from the authentication response.
     * @param response The credential response from Google authentication
     * @return GoogleAuthResult containing the extracted credentials
     */
    fun extractAndStoreCredentials(response: GetCredentialResponse): GoogleAuthResult? {
        return try {
            val credential = response.credential

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                extractGoogleIdTokenCredential(credential)
            } else {
                Timber.e("Unexpected credential type: ${credential.type}")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to extract credentials")
            null
        }
    }

    /**
     * Extracts Google ID token credential from custom credential.
     * @param credential The custom credential containing Google ID token
     * @return GoogleAuthResult with extracted credentials
     */
    private fun extractGoogleIdTokenCredential(credential: CustomCredential): GoogleAuthResult? {
        return try {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken
            val googleUserId = googleIdTokenCredential.id

            // Store credentials for later use
            storedGoogleIdToken = idToken
            storedGoogleUserId = googleUserId

            GoogleAuthResult(
                idToken = idToken,
                googleUserId = googleUserId
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to create GoogleIdTokenCredential")
            null
        }
    }

    /**
     * Returns the stored Google credentials if available.
     * @return GoogleAuthResult with stored credentials, null if not available
     */
    fun getStoredCredentials(): GoogleAuthResult? {
        return if (storedGoogleIdToken != null && storedGoogleUserId != null) {
            GoogleAuthResult(
                idToken = storedGoogleIdToken!!,
                googleUserId = storedGoogleUserId!!
            )
        } else {
            null
        }
    }

    /**
     * Clears stored Google credentials.
     */
    fun clearStoredCredentials() {
        storedGoogleIdToken = null
        storedGoogleUserId = null
    }

    /**
     * Checks if Google credentials are currently stored.
     * @return true if credentials are stored, false otherwise
     */
    fun hasStoredCredentials(): Boolean {
        return storedGoogleIdToken != null && storedGoogleUserId != null
    }

    /**
     * Authenticates with Firebase using the Google ID token.
     * @param idToken The Google ID token to authenticate with
     * @return FirebaseUser if successful, null otherwise
     */
    suspend fun firebaseAuthWithGoogle(idToken: String): FirebaseUser? {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()

            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                firebaseUser
            } else {
                Timber.e("Firebase authentication failed - No user returned")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Firebase authentication failed - ${e.localizedMessage}")
            throw e
        }
    }

    /**
     * Signs out the user from both Firebase and clears credential state.
     */
    override suspend fun signOut() {
        Timber.tag(TAG).d("signOut() called - starting logout process")
        try {
            // Firebase sign out
            Timber.tag(TAG).d("Signing out from Firebase...")
            firebaseAuth.signOut()

            // Clear stored credentials
            clearStoredCredentials()

            // Clear credential state from Credential Manager
            Timber.tag(TAG).d("Clearing credential state from Credential Manager...")
            val clearRequest = ClearCredentialStateRequest()
            credentialManager.clearCredentialState(clearRequest)

            Timber.tag(TAG).d("User signed out successfully")
        } catch (e: ClearCredentialException) {
            Timber.tag(TAG).e(e, "Failed to clear credential state: ${e.localizedMessage}")
            throw e
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Sign out failed")
            throw e
        }
    }

    /**
     * Gets the current Firebase user.
     * @return FirebaseUser if signed in, null otherwise
     */
    fun getCurrentFirebaseUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    /**
     * Refreshes the Firebase ID token for the current user.
     * @param forceRefresh Whether to force refresh the token even if it's not expired
     * @return Fresh Firebase ID token if successful
     * @throws IllegalStateException if user is not signed in
     * @throws Exception if token refresh fails
     */
    override suspend fun refreshFirebaseIdToken(forceRefresh: Boolean): String {
        val currentUser = firebaseAuth.currentUser
        Timber.tag(TAG).d("refreshFirebaseIdToken(forceRefresh=$forceRefresh) - currentUser: ${currentUser?.uid}")

        if (currentUser == null) {
            Timber.tag(TAG).e("Cannot refresh token - no Firebase user")
            throw IllegalStateException("User is not signed in to Firebase")
        }

        return try {
            val tokenResult = currentUser.getIdToken(forceRefresh).await()
            val token = tokenResult?.token
                ?: throw IllegalStateException("Failed to get Firebase ID token")

            Timber.tag(TAG).d("Firebase ID token refreshed successfully (length=${token.length})")
            token
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to refresh Firebase ID token")
            throw e
        }
    }
}

/**
 * Data class representing Google authentication result.
 */
data class GoogleAuthResult(
    val idToken: String,
    val googleUserId: String
)
