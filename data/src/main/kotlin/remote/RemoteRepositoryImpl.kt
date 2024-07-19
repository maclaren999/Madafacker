package remote

import com.bbuddies.gotogether.common_domain.model.Message
import com.bbuddies.gotogether.common_domain.model.Reply
import com.bbuddies.gotogether.common_domain.model.User
import com.bbuddies.gotogether.common_domain.repository.RemoteRepository
import remote.api.RetrofitInstance

class RemoteRepositoryImpl() : RemoteRepository {
    private val webService = RetrofitInstance.madafakerWebService

    override suspend fun getCurrentUser(): User {
        return webService.getCurrentUser()
    }

    override suspend fun getIncomingMassage(): List<Message> {
        return webService.getIncomingMassage()
    }

    override suspend fun getOutcomingMassage(): List<Message> {
        return webService.getOutcomingMassage()
    }

    override suspend fun getReplyById(id: String): Reply {
        return webService.getReplyById(id)
    }

    override suspend fun updateCurrentUser(name: String): User {
        return webService.updateCurrentUser(name)
    }

    override suspend fun updateReply(id: String, isPublic: Boolean) {
        return webService.updateReply(id, isPublic)
    }

    override suspend fun createUser(name: String): User {
        return webService.createUser(name)
    }

    override suspend fun createMessage(body: String, mode: String): Message {
        return webService.createMessage(body, mode)
    }

    override suspend fun createReply(body: String?, isPublic: Boolean, parentId: String?) {
        return webService.createReply(body, isPublic, parentId) //TODO
    }
}