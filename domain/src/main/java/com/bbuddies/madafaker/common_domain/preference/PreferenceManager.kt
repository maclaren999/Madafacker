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
    val currentMode: StateFlow<Mode>

    suspend fun updateGoogleUserId(googleUserId: String)
    suspend fun updateGoogleIdToken(googleIdToken: String)
    suspend fun updateFirebaseIdToken(firebaseIdToken: String)
    suspend fun updateAllAuthTokens(googleIdToken: String, googleUserId: String, firebaseIdToken: String)
    suspend fun updateCurrentMode(mode: Mode)
    suspend fun updateMode(mode: Mode)
    suspend fun clearUserData()
}