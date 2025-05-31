package remote

import com.bbuddies.madafaker.common_domain.model.User
import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import com.bbuddies.madafaker.common_domain.repository.UserRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import remote.api.MadafakerApi
import remote.api.request.CreateUserRequest
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val webService: MadafakerApi,
    private val preferenceManager: PreferenceManager,
) : UserRepository {

    val firebaseMessaging by lazy { FirebaseMessaging.getInstance() }

    override suspend fun getCurrentUser(): User? = withContext(Dispatchers.IO) {
        preferenceManager.authToken.value?.let {
            webService.getCurrentUser() //authToken is been added to the header inside [AuthInterceptor]
        }
    }

    override suspend fun updateUserName(name: String): User = withContext(Dispatchers.IO) {
        webService.updateCurrentUser(name)
    }

    override suspend fun createUser(name: String): User = withContext(Dispatchers.IO) {
        val fcmToken = firebaseMessaging.token.await()
        val user = webService.createUser(CreateUserRequest(name, fcmToken))
        preferenceManager.updateAuthToken(user.id) //fcmToken = User.id = authToken
        user
    }

    override suspend fun isNameAvailable(name: String): Boolean = withContext(Dispatchers.IO) {
        webService.checkNameAvailability(name).nameIsAvailable
    }
}