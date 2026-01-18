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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AUTH_MANAGER"

/**
 * Manages Google authentication operations including credential management,
 * token extraction, and authentication flow.
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

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        val newStatus = if (auth.currentUser != null) {
            FirebaseAuthStatus.SignedIn
        } else {
            FirebaseAuthStatus.SignedOut
        }
        Timber.tag(TAG)
            .d("Firebase AuthStateListener: ${_firebaseStatus.value} -> $newStatus (user=${auth.currentUser?.uid})")
        _firebaseStatus.value = newStatus
    }

    init {
        Timber.tag(TAG).d("GoogleAuthManager init - current Firebase user: ${firebaseAuth.currentUser?.uid}")
        firebaseAuth.addAuthStateListener(authStateListener)
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
