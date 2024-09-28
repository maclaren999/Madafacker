package remote

import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.Reply
import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import com.bbuddies.madafaker.common_domain.repository.MessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.withContext
import local.MadafakerDao
import local.entity.MessageDB
import remote.api.MadafakerApi
import javax.inject.Inject

class MessageRepositoryImpl @Inject internal constructor(
    private val webService: MadafakerApi,
    private val preferenceManager: PreferenceManager,
    private val localDao: MadafakerDao
) :
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

    override suspend fun createMessage(body: String): Message =
        withContext(Dispatchers.IO) {
            val currentMode = preferenceManager.currentMode.last()
            var newMessage = webService.createMessage(body, currentMode.name)
            localDao.insertMessage(newMessage.asMessageDB())
            newMessage
        }

    override suspend fun createReply(body: String?, isPublic: Boolean, parentId: String?) {
        return webService.createReply(body, isPublic, parentId) //TODO
    }

}

fun MessageDB.asMessage() = Message(
    id = id,
    body = body,
    mode = mode,
    createdAt = createdAt,
    isPublic = isPublic,
    authorId = authorId
)

fun Message.asMessageDB() = MessageDB(
    id = id,
    body = body,
    mode = mode,
    createdAt = createdAt,
    isPublic = isPublic,
    authorId = authorId

)