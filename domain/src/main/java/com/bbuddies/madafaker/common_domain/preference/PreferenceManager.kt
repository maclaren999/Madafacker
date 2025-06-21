package com.bbuddies.madafaker.common_domain.preference

import com.bbuddies.madafaker.common_domain.enums.Mode
import kotlinx.coroutines.flow.StateFlow

/**
 * Preference Manager for the application.
 * Handles auth token and simple app preferences only.
 * User data is managed by UserRepository/Room DB.
 */
interface PreferenceManager {
    val authToken: StateFlow<String?>
    val currentMode: StateFlow<Mode>

    suspend fun updateAuthToken(authToken: String)
    suspend fun updateCurrentMode(mode: Mode)
    suspend fun updateMode(mode: Mode)
    suspend fun clearUserData()
}