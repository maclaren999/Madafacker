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
    suspend fun updateCurrentUser(name: String): User
    suspend fun createUser(name: String): User

}