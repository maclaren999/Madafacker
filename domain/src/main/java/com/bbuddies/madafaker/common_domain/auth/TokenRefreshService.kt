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
     */
    val firebaseStatus: StateFlow<FirebaseAuthStatus>

    /**
     * @deprecated Use firebaseStatus instead
     */
    @Deprecated("Use firebaseStatus", ReplaceWith("firebaseStatus"))
    val authState: StateFlow<FirebaseAuthStatus>
        get() = firebaseStatus

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
     */
    fun hasFirebaseUser(): Boolean
}
