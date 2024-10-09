package remote

import com.bbuddies.madafaker.common_domain.model.User
import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import com.bbuddies.madafaker.common_domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.withContext
import remote.api.MadafakerApi
import remote.api.request.CreateUserRequest
import javax.inject.Inject

class UserRepositoryImpl @Inject internal constructor(
    private val webService: MadafakerApi,
    private val preferenceManager: PreferenceManager,
//    private val localDatabase:
) : UserRepository {

    override suspend fun getCurrentUser(): User? = withContext(Dispatchers.IO) {
        preferenceManager.authToken.last()?.let {
            webService.getCurrentUser() //authToken is been added to the header inside [AuthInterceptor]
        }
    }

    override suspend fun updateUserName(name: String): User {
        return webService.updateCurrentUser(name)
    }

    override suspend fun createUser(name: String,fcmToken: String): User = withContext(Dispatchers.IO) {
        val user = webService.createUser(CreateUserRequest(name, fcmToken))
        preferenceManager.updateAuthToken(user.id)
        user
    }

    override suspend fun isNameAvailable(name: String): Boolean = withContext(Dispatchers.IO) {
        webService.checkNameAvailability(name).nameIsAvailable
    }
}