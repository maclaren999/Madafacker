package repository

import com.bbuddies.madafaker.common_domain.auth.TokenRefreshService
import com.bbuddies.madafaker.common_domain.model.AuthenticationState
import com.bbuddies.madafaker.common_domain.model.User
import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import com.bbuddies.madafaker.common_domain.repository.UserRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import local.MadafakerDao
import remote.api.MadafakerApi
import remote.api.request.CreateUserRequest
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val webService: MadafakerApi,
    private val preferenceManager: PreferenceManager,
    private val localDao: MadafakerDao, // Add this for local caching
    private val tokenRefreshService: TokenRefreshService
) : UserRepository {

    val firebaseMessaging by lazy { FirebaseMessaging.getInstance() }
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val authenticationState: StateFlow<AuthenticationState> =
        preferenceManager.googleIdAuthToken
            .map { authToken ->
                when {
                    authToken == null -> AuthenticationState.NotAuthenticated
                    else -> {
                        try {
                            val user = localDao.getUserById(authToken)
                            if (user != null) {
                                AuthenticationState.Authenticated(user)
                            } else {
                                // Try to fetch from network
                                val networkUser = webService.getCurrentUser()
                                localDao.insertUser(networkUser)
                                AuthenticationState.Authenticated(networkUser)
                            }
                        } catch (e: Exception) {
                            AuthenticationState.Error(e)
                        }
                    }
                }
            }
            .stateIn(
                scope = repositoryScope,
                started = SharingStarted.Eagerly,
                initialValue = AuthenticationState.Loading
            )

    override val currentUser: StateFlow<User?> = authenticationState
        .map { state ->
            when (state) {
                is AuthenticationState.Authenticated -> state.user
                is AuthenticationState.NotAuthenticated -> null
                is AuthenticationState.Error -> null
                is AuthenticationState.Loading -> return@map null // Don't emit during loading
            }
        }
        .distinctUntilChanged() // Only emit when user actually changes
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly, // Start immediately to avoid initial null
            initialValue = null
        )

    // Add a suspend function that waits for authentication to complete
    override suspend fun awaitCurrentUser(): User? {
        return authenticationState
            .filterNot { it is AuthenticationState.Loading } // Skip loading states
            .map { state ->
                when (state) {
                    is AuthenticationState.Authenticated -> state.user
                    else -> null
                }
            }
            .first() // Get the first non-loading result
    }

    override val isUserLoggedIn: StateFlow<Boolean> = authenticationState
        .map { it is AuthenticationState.Authenticated }
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    override suspend fun getCurrentUserOrThrow(): User =
        when (val state = authenticationState.value) {
            is AuthenticationState.Authenticated -> state.user
            is AuthenticationState.NotAuthenticated -> throw IllegalStateException("User not authenticated")
            is AuthenticationState.Loading -> throw IllegalStateException("Authentication state is loading")
            is AuthenticationState.Error -> throw state.exception
        }

    override suspend fun getCurrentUser(): User? = withContext(Dispatchers.IO) {
        // Cache-only lookup - network fetching is handled by authenticationState flow
        preferenceManager.googleIdAuthToken.value?.let { token ->
            try {
                localDao.getUserById(token)
            } catch (exception: Exception) {
                Timber.e(exception, "Error getting user from cache")
                null
            }
        }
    }

    override suspend fun updateUserName(name: String): User = withContext(Dispatchers.IO) {
        val updatedUser = webService.updateCurrentUser(name)
        // Update local cache
        localDao.insertUser(updatedUser) // Direct use of domain model
        updatedUser
    }

//    override suspend fun createUser(name: String): User = withContext(Dispatchers.IO) {
//        // First attempt with current FCM token
//        val initialFcmToken = firebaseMessaging.token.await()
//
//        try {
//            val user = webService.createUser(CreateUserRequest(name, initialFcmToken))
//            preferenceManager.updateAuthToken(user.id)
//            localDao.insertUser(user)
//            Timber.tag("USER_REPO").d("User created with FCM token: $initialFcmToken")
//            return@withContext user
//        } catch (e: Exception) {
//            // Check if it's a duplicate registration token error
//            if (isDuplicateRegistrationTokenError(e)) {
//                // Refresh FCM token and retry
//                val freshFcmToken = refreshFcmToken()
//                try {
//                    val user = webService.createUser(CreateUserRequest(name, freshFcmToken))
//                    preferenceManager.updateAuthToken(user.id)
//                    localDao.insertUser(user)
//                    return@withContext user
//                } catch (retryException: Exception) {
//                    // If retry also fails, throw the retry exception
//                    throw retryException
//                }
//            } else {
//                // If it's not a duplicate token error, rethrow original exception
//                throw e
//            }
//        }
//    }

    override suspend fun isNameAvailable(name: String): Boolean = withContext(Dispatchers.IO) {
        webService.checkNameAvailability(name).nameIsAvailable
    }

    override suspend fun clearAllUserData() = withContext(Dispatchers.IO) {
        try {
            // Delete current FCM token to ensure fresh token for new account
            firebaseMessaging.deleteToken().await()
        } catch (e: Exception) {
            // Log but don't fail the logout process if token deletion fails
            // The user should still be able to logout even if FCM cleanup fails
        }

        // Clear all local data
        localDao.clearAllData()
        // Clear preferences
        preferenceManager.clearUserData()
    }

    private suspend fun refreshFcmToken(): String = withContext(Dispatchers.IO) {
        try {
            // Delete current token
            firebaseMessaging.deleteToken().await()
            // Get new token
            firebaseMessaging.token.await()
        } catch (e: Exception) {
            // Fallback to just getting current token if deletion fails
            firebaseMessaging.token.await()
        }
    }

    override suspend fun storeGoogleAuth(
        googleIdToken: String,
        googleUserId: String,
        firebaseIdToken: String,
        firebaseUid: String
    ) = withContext(Dispatchers.IO) {
        preferenceManager.updateAllAuthTokens(googleIdToken, googleUserId, firebaseIdToken, firebaseUid)
        Timber.tag("USER_REPO")
            .d("Auth googleIdToken: $googleIdToken \n googleUserId: $googleUserId \n firebaseIdToken: $firebaseIdToken \n firebaseUid: $firebaseUid")
    }

    override suspend fun refreshFirebaseIdToken(): String = withContext(Dispatchers.IO) {
        try {
            // Get fresh token from Firebase
            val newToken = tokenRefreshService.refreshFirebaseIdToken()

            // Update stored token
            preferenceManager.updateFirebaseIdToken(newToken)

            Timber.tag("USER_REPO").d("Firebase ID token refreshed and updated in preferences")
            newToken
        } catch (e: Exception) {
            Timber.e(e, "Failed to refresh Firebase ID token")
            throw e
        }
    }



    override suspend fun createUserWithGoogle(nickname: String, idToken: String, googleUserId: String): User =
        withContext(Dispatchers.IO) {
            try {
                val fcmToken = getFreshFcmToken()
                attemptUserCreation(nickname, fcmToken, idToken)
            } catch (e: Exception) {
                Timber.e(e, "Failed to create user for googleUserId: $googleUserId")
                throw e
            }
        }

    /**
     * Attempts to create a user with the given parameters, handling FCM token conflicts.
     * @param nickname User's chosen nickname
     * @param fcmToken FCM token for notifications
     * @param idToken Google ID token for authentication
     * @return Created User object
     */
    private suspend fun attemptUserCreation(nickname: String, fcmToken: String, idToken: String): User {
        return try {
            val user = webService.createUser(CreateUserRequest(nickname, fcmToken))
            // Note: We only update googleIdToken here since all tokens were already stored in storeGoogleAuth
            preferenceManager.updateAuthToken(idToken)
            localDao.insertUser(user)
            user
        } catch (e: Exception) {
            if (isDuplicateRegistrationTokenError(e)) {
                Timber.w("FCM token conflict detected, retrying with fresh token")
                retryUserCreationWithFreshToken(nickname, idToken)
            } else {
                throw e
            }
        }
    }

    /**
     * Retries user creation with a fresh FCM token.
     * @param nickname User's chosen nickname
     * @param idToken Google ID token for authentication
     * @return Created User object
     */
    private suspend fun retryUserCreationWithFreshToken(nickname: String, idToken: String): User {
        val freshFcmToken = refreshFcmToken()
        val user = webService.createUser(CreateUserRequest(nickname, freshFcmToken))
        preferenceManager.updateAuthToken(idToken)
        localDao.insertUser(user)
        return user
    }

    /**
     * Gets a fresh FCM token, attempting to refresh if needed.
     * @return FCM token string
     */
    private suspend fun getFreshFcmToken(): String {
        return try {
            firebaseMessaging.token.await()
        } catch (e: Exception) {
            Timber.w(e, "Failed to get initial FCM token, attempting refresh")
            refreshFcmToken()
        }
    }

    override suspend fun authenticateWithGoogle(googleIdToken: String, googleUserId: String): User =
        withContext(Dispatchers.IO) {
            try {
                // For existing users, get current user info using Firebase ID token in header
                val user = webService.getCurrentUser()

                // Update local storage
                preferenceManager.updateAuthToken(googleIdToken)
                localDao.insertUser(user)

                user
            } catch (e: Exception) {
                Timber.e(e, "Authentication failed for googleUserId: $googleUserId")
                throw e
            }
        }

}

private fun isDuplicateRegistrationTokenError(exception: Exception): Boolean {
    return when (exception) {
        is HttpException -> {
            if (exception.code() != 400) return false

            try {
                val errorBody = exception.response()?.errorBody()?.string()
                if (errorBody != null) {
                    // Parse JSON response to check for duplicate registration token error
                    val isRegistrationTokenError = errorBody.contains("registrationToken") &&
                            errorBody.contains("already exists")
                    val isDuplicatedValueError = errorBody.contains("Duplicated value is not allowed")

                    return isRegistrationTokenError || isDuplicatedValueError
                }
                false
            } catch (e: Exception) {
                false
            }
        }

        else -> {
            val message = exception.message?.lowercase() ?: ""
            message.contains("registrationtoken") &&
                    (message.contains("already exists") || message.contains("duplicate"))
        }
    }


}
