package com.bbuddies.madafaker.common_domain.preference

import kotlinx.coroutines.flow.StateFlow


/**
 * Preference Manager for the application.
 */
interface PreferenceManager {

    val authToken: StateFlow<String?>

    suspend fun updateAuthToken(authToken: String)

}