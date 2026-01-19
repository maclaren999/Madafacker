package com.bbuddies.madafaker.common_domain.auth

import kotlinx.coroutines.flow.StateFlow

/**
 * Service for managing Firebase authentication and token refresh.
 * This interface allows the data layer to interact with Firebase without depending on presentation layer.
 */
interface TokenRefreshService {
    /**
     * Stream of Firebase auth status.
     * This represents Firebase's internal state, not the app's login state.
     *
     * IMPORTANT: On cold start, Firebase may take time to restore session.
     * The status will be:
     * - Initializing: Firebase hasn't called AuthStateListener yet
     * - SignedOut: Firebase called listener with null user (may be temporary on cold start)
     * - SignedIn: Firebase confirmed user is signed in
     *
     * Use [awaitInitialization] to wait for Firebase to finish initializing.
     */
    val firebaseStatus: StateFlow<FirebaseAuthStatus>

    /**
     * Waits for Firebase to complete its initial auth state restoration.
     * On cold start, Firebase may report SignedOut before finishing initialization.
     * This method waits up to [timeoutMs] for Firebase to settle.
     *
     * @param timeoutMs Maximum time to wait for initialization
     * @return The confirmed FirebaseAuthStatus after initialization
     */
    suspend fun awaitInitialization(timeoutMs: Long = 5000): FirebaseAuthStatus

    /**
     * Refreshes the Firebase ID token for the current user.
     * @param forceRefresh Whether to force refresh the token even if it's not expired
     * @return Fresh Firebase ID token if successful
     * @throws IllegalStateException if Firebase has no signed-in user
     * @throws Exception if token refresh fails
     */
    suspend fun refreshFirebaseIdToken(forceRefresh: Boolean = true): String

    /**
     * Signs out from Firebase and clears credential state.
     * This should be called during explicit user logout.
     */
    suspend fun signOut()

    /**
     * Checks if Firebase currently has a signed-in user.
     * This is a quick synchronous check.
     *
     * NOTE: On cold start, this may return false even if user will be restored.
     * Use [awaitInitialization] if you need a reliable check.
     */
    fun hasFirebaseUser(): Boolean

    /**
     * Attempts to restore Firebase session using stored Google ID token.
     * Call this when Firebase has no user but we have stored credentials.
     *
     * WARNING: Google ID tokens expire after ~1 hour. This will fail with expired tokens.
     *
     * @param googleIdToken The stored Google ID token
     * @return true if restoration successful, false otherwise
     */
    suspend fun restoreFirebaseSession(googleIdToken: String): Boolean
}
