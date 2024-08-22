package remote

import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.Reply
import com.bbuddies.madafaker.common_domain.repository.MessageRepository
import remote.api.MadafakerApi
import javax.inject.Inject

class MessageRepositoryImpl @Inject internal constructor(private val webService: MadafakerApi) :
    MessageRepository {

    override suspend fun getIncomingMassage(): List<Message> {
        return webService.getIncomingMassage()
    }

    override suspend fun getOutcomingMassage(): List<Message> {
        return webService.getOutcomingMassage()
    }

    override suspend fun getReplyById(id: String): Reply {
        return webService.getReplyById(id)
    }

    override suspend fun updateReply(id: String, isPublic: Boolean) {
        return webService.updateReply(id, isPublic)
    }

    override suspend fun createMessage(body: String, mode: String): Message {
        return webService.createMessage(body, mode)
    }

    override suspend fun createReply(body: String?, isPublic: Boolean, parentId: String?) {
        return webService.createReply(body, isPublic, parentId) //TODO
    }

}