package com.bbuddies.madafaker.presentation.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.bbuddies.madafaker.presentation.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages Google authentication operations including credential management,
 * token extraction, and authentication flow.
 */
@Singleton
class GoogleAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
    private val credentialManager = CredentialManager.create(context)

    // Store Google credentials for later use in account creation
    private var storedGoogleIdToken: String? = null
    private var storedGoogleUserId: String? = null

    /**
     * Performs Google authentication and returns the credential response.
     * @return GetCredentialResponse if successful, null otherwise
     * @throws GetCredentialException if authentication fails
     */
    suspend fun performGoogleAuthentication(): GetCredentialResponse? {
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
                context = context
            )
        } catch (e: GetCredentialException) {
            Timber.e(e, "Google authentication failed")
            throw e
        }
    }

    /**
     * Extracts and stores Google credentials from the authentication response.
     * @param response The credential response from Google authentication
     * @return GoogleAuthResult containing the extracted credentials
     */
    fun extractAndStoreCredentials(response: GetCredentialResponse): GoogleAuthResult? {
        val credential = response.credential

        return if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {

            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val googleUserId = googleIdTokenCredential.id

                // Store credentials for later use
                storedGoogleIdToken = idToken
                storedGoogleUserId = googleUserId

                Timber.d("Google Sign-In successful. User ID: $googleUserId")

                GoogleAuthResult(
                    idToken = idToken,
                    googleUserId = googleUserId
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to extract Google credentials")
                null
            }
        } else {
            Timber.e("Unexpected credential type: ${credential.type}")
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
}

/**
 * Data class representing Google authentication result.
 */
data class GoogleAuthResult(
    val idToken: String,
    val googleUserId: String
)
