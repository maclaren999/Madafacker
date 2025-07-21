package com.bbuddies.madafaker.common_domain.auth

/**
 * Service for refreshing Firebase ID tokens.
 * This interface allows the data layer to refresh tokens without depending on presentation layer.
 */
interface TokenRefreshService {
    /**
     * Refreshes the Firebase ID token for the current user.
     * @param forceRefresh Whether to force refresh the token even if it's not expired
     * @return Fresh Firebase ID token if successful
     * @throws IllegalStateException if user is not signed in
     * @throws Exception if token refresh fails
     */
    suspend fun refreshFirebaseIdToken(forceRefresh: Boolean = true): String

    /**
     * Checks if user is currently signed in to Firebase.
     * @return true if signed in, false otherwise
     */
    fun isSignedIn(): Boolean
}
