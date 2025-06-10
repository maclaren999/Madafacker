package com.bbuddies.madafaker.common_domain.preference

import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.User
import kotlinx.coroutines.flow.StateFlow


/**
 * Preference Manager for the application.
 */
interface PreferenceManager {

    val authToken: StateFlow<String?>
    val currentMode: StateFlow<Mode>
    val currentUser: StateFlow<User?>

    suspend fun updateAuthToken(authToken: String)
    suspend fun updateCurrentMode(mode: Mode)
    suspend fun updateCurrentUser(user: User)
    suspend fun clearUserData()
    suspend fun updateMode(mode: Mode)

}