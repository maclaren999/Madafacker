package repository

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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val webService: MadafakerApi,
    private val preferenceManager: PreferenceManager,
    private val localDao: MadafakerDao // Add this for local caching
) : UserRepository {

    val firebaseMessaging by lazy { FirebaseMessaging.getInstance() }
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val authenticationState: StateFlow<AuthenticationState> =
        preferenceManager.authToken
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
                started = SharingStarted.Lazily,
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
            started = SharingStarted.Lazily,
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
        preferenceManager.authToken.value?.let { token ->
            try {
                val user = webService.getCurrentUser() // authToken added in AuthInterceptor
                // Cache user locally
                localDao.insertUser(user) // Direct use of domain model
                user
            } catch (exception: Exception) {
                // Fallback to local cache if network fails
                localDao.getUserById(token)
            }
        }
    }

    override suspend fun updateUserName(name: String): User = withContext(Dispatchers.IO) {
        val updatedUser = webService.updateCurrentUser(name)
        // Update local cache
        localDao.insertUser(updatedUser) // Direct use of domain model
        updatedUser
    }

    override suspend fun createUser(name: String): User = withContext(Dispatchers.IO) {
        val fcmToken = firebaseMessaging.token.await()
        val user = webService.createUser(CreateUserRequest(name, fcmToken))
        preferenceManager.updateAuthToken(user.id) // fcmToken = User.id = authToken
        // Cache user locally
        localDao.insertUser(user) // Direct use of domain model
        user
    }

    override suspend fun isNameAvailable(name: String): Boolean = withContext(Dispatchers.IO) {
        webService.checkNameAvailability(name).nameIsAvailable
    }

    override suspend fun clearAllUserData() = withContext(Dispatchers.IO) {
        // Clear all local data
        localDao.clearAllData()
        // Clear preferences
        preferenceManager.clearUserData()
    }
}