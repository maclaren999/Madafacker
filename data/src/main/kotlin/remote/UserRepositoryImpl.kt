package remote

import com.bbuddies.madafaker.common_domain.model.User
import com.bbuddies.madafaker.common_domain.repository.UserRepository
import remote.api.MadafakerApi
import javax.inject.Inject

class UserRepositoryImpl @Inject internal constructor(private val webService: MadafakerApi) : UserRepository {

    override suspend fun getCurrentUser(): User {
        return webService.getCurrentUser()
    }

    override suspend fun updateCurrentUser(name: String): User {
        return webService.updateCurrentUser(name)
    }

    override suspend fun createUser(name: String): User {
        return webService.createUser(name)
    }
}