package com.bbuddies.madafaker.common_domain.preference

import kotlinx.coroutines.flow.Flow


/**
 * Preference Manager for the application.
 */
interface PreferenceManager {

    val authToken: Flow<String?>

    suspend fun updateAuthToken(authToken: String)

}