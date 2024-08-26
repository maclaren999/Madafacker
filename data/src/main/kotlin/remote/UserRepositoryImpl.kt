package remote

import com.bbuddies.madafaker.common_domain.model.User
import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import com.bbuddies.madafaker.common_domain.repository.UserRepository
import kotlinx.coroutines.flow.last
import remote.api.MadafakerApi
import javax.inject.Inject

class UserRepositoryImpl @Inject internal constructor(
    private val webService: MadafakerApi,
    private val preferenceManager: PreferenceManager,
//    private val localDatabase:
) : UserRepository {

    override suspend fun getCurrentUser(): User? {
//        getLocalUser()?.let { return it } //TODO: Impl local db

        return preferenceManager.authToken.last()?.let {
            webService.getCurrentUser()
        } // ?: createUser("") //when server will allow user with empty name
    }

    override suspend fun updateCurrentUser(name: String): User {
        return webService.updateCurrentUser(name)
    }

    override suspend fun createUser(name: String): User {
        return webService.createUser(name)
    }
}