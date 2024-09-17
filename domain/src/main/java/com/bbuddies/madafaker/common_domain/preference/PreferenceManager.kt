package com.bbuddies.madafaker.common_domain.preference

import com.bbuddies.madafaker.common_domain.enums.Mode
import kotlinx.coroutines.flow.StateFlow


/**
 * Preference Manager for the application.
 */
interface PreferenceManager {

    val authToken: StateFlow<String?>
    val currentMode: Flow<Mode>

    suspend fun updateAuthToken(authToken: String)
    suspend fun updateCurrentMode(mode:Mode)


}