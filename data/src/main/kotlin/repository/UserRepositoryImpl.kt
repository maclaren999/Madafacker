package repository

import com.bbuddies.madafaker.common_domain.model.User
import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import com.bbuddies.madafaker.common_domain.repository.UserRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import local.MadafakerDao
import remote.api.MadafakerApi
import remote.api.request.CreateUserRequest
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val webService: MadafakerApi,
    private val preferenceManager: PreferenceManager,
    private val localDao: MadafakerDao // Add this for local caching
) : UserRepository {

    val firebaseMessaging by lazy { FirebaseMessaging.getInstance() }

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
}