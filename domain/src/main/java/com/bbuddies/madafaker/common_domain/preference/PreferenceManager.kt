package com.bbuddies.madafaker.common_domain.preference

import com.bbuddies.madafaker.common_domain.enums.Mode
import kotlinx.coroutines.flow.Flow


/**
 * Preference Manager for the application.
 */
interface PreferenceManager {

    val authToken: Flow<String?>
    val currentMode: Flow<String?>


    suspend fun updateAuthToken(authToken: String)
    suspend fun updateCurrentMode(mode:Mode)


}