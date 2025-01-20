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

    /**
     * Creates a user on the server, handles FCM token retrieval internally,
     * and saves the user model and auth token locally.
     */
    suspend fun createUser(name: String): User

    suspend fun updateUserName(name: String): User

    /**
     * Requests server to check if name is available, not taken.
     * */
    suspend fun isNameAvailable(name: String): Boolean

}