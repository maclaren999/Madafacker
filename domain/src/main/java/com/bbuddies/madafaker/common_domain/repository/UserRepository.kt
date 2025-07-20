package com.bbuddies.madafaker.common_domain.repository

import com.bbuddies.madafaker.common_domain.model.AuthenticationState
import com.bbuddies.madafaker.common_domain.model.User
import kotlinx.coroutines.flow.StateFlow

/**
 * UserRepository interface provides methods to interact with User data.
 * And encapsulates auth logic.
 */
interface UserRepository {

    /**
     * Observable authentication state with user data
     */
    val authenticationState: StateFlow<AuthenticationState>

    /**
     * Convenience property for current user (nullable)
     */
    val currentUser: StateFlow<User?>

    /**
     * Observable login state
     */
    val isUserLoggedIn: StateFlow<Boolean>

    /**
     * Emits null if user is not logged in.
     */
    suspend fun awaitCurrentUser(): User?

    /**
     * Gets current authenticated user or throws if not authenticated
     */
    suspend fun getCurrentUserOrThrow(): User

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
//    suspend fun createUser(name: String): User

    suspend fun updateUserName(name: String): User

    /**
     * Requests server to check if name is available, not taken.
     * */
    suspend fun isNameAvailable(name: String): Boolean

    suspend fun clearAllUserData()

    suspend fun storeGoogleAuth(googleIdToken: String, googleUserId: String, firebaseIdToken: String)
    suspend fun createUserWithGoogle(nickname: String, idToken: String, googleUserId: String): User
    suspend fun authenticateWithGoogle(googleIdToken: String, googleUserId: String): User
}