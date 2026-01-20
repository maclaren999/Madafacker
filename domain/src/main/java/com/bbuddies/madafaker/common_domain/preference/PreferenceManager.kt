package com.bbuddies.madafaker.common_domain.preference

import com.bbuddies.madafaker.common_domain.enums.Mode
import kotlinx.coroutines.flow.StateFlow

/**
 * Preference Manager for the application.
 * Handles auth token and simple app preferences only.
 * User data is managed by UserRepository/Room DB.
 */
interface PreferenceManager {
    val googleIdAuthToken: StateFlow<String?>
    val firebaseIdToken: StateFlow<String?>
    val googleUserId: StateFlow<String?>
    val firebaseUid: StateFlow<String?>
    val userId: StateFlow<String?>
    val currentMode: StateFlow<Mode>

    /**
     * Indicates whether user has an active login session.
     * This is set to true on successful login and only cleared on explicit logout
     * or confirmed authentication failure (not on Firebase state changes).
     */
    val isSessionActive: StateFlow<Boolean>

    suspend fun updateAuthToken(googleIdToken: String)
    suspend fun updateFirebaseIdToken(firebaseIdToken: String)
    suspend fun updateGoogleUserId(googleUserId: String)
    suspend fun updateFirebaseUid(firebaseUid: String)
    suspend fun updateUserId(userId: String)
    suspend fun updateAllAuthTokens(
        googleIdToken: String,
        googleUserId: String,
        firebaseIdToken: String,
        firebaseUid: String
    )
    suspend fun updateCurrentMode(mode: Mode)
    suspend fun updateMode(mode: Mode)

    /**
     * Sets the session active flag. Call with true on successful login,
     * and false only on explicit logout or confirmed auth failure.
     */
    suspend fun setSessionActive(active: Boolean)

    /**
     * Clears all user data including session state.
     * Should be called on logout.
     */
    suspend fun clearUserData()
}
