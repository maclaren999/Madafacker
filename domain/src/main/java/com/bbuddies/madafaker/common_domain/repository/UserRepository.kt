package com.bbuddies.madafaker.common_domain.repository

import com.bbuddies.madafaker.common_domain.model.User

/**
 * UserRepository interface provides methods to interact with User data.
 * And encapsulates auth logic.
 */
interface UserRepository {

    /**
     * Checks local storage for User entity.
     * If User is not found, it will fetch from the server.
     *
     * Returns null if user is not found.
     * */
    suspend fun getCurrentUser(): User?

    suspend fun updateUserName(name: String): User

    /**
     * Creates User on the server and saves User model and auth token in local storage.
     * */
    suspend fun createUser(name: String, fcmToken: String): User

    /**
     * Requests server to check if name is available, not taken.
     * */
    suspend fun isNameAvailable(name: String): Boolean

}