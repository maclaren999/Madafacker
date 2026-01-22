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
    val currentMode: StateFlow<Mode>
    val notificationPermissionPromptDismissed: StateFlow<Boolean>
    val hasSeenModeToggleTip: StateFlow<Boolean>

    suspend fun updateAuthToken(googleIdToken: String)
    suspend fun updateFirebaseIdToken(firebaseIdToken: String)
    suspend fun updateGoogleUserId(googleUserId: String)
    suspend fun updateFirebaseUid(firebaseUid: String)
    suspend fun updateAllAuthTokens(
        googleIdToken: String,
        googleUserId: String,
        firebaseIdToken: String,
        firebaseUid: String
    )
    suspend fun updateCurrentMode(mode: Mode)
    suspend fun updateMode(mode: Mode)
    suspend fun setHasSeenModeToggleTip(seen: Boolean)
    suspend fun setNotificationPermissionPromptDismissed(dismissed: Boolean)
    suspend fun clearUserData()
}
