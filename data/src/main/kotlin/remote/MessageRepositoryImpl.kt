package remote

import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.Reply
import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import com.bbuddies.madafaker.common_domain.repository.MessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import local.MadafakerDao
import local.entity.MessageDB
import remote.api.MadafakerApi
import remote.api.request.CreateMessageRequest
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val webService: MadafakerApi,
    private val preferenceManager: PreferenceManager,
    private val localDao: MadafakerDao
) : MessageRepository {

    override suspend fun getIncomingMassage(): List<Message> =
        withContext(Dispatchers.IO) {
            webService.getIncomingMassage()
        }

    override suspend fun getOutcomingMassage(): List<Message> =
        withContext(Dispatchers.IO) {
            webService.getOutcomingMassage()
        }

    override suspend fun getReplyById(id: String): Reply =
        withContext(Dispatchers.IO) {
            webService.getReplyById(id)
        }

    override suspend fun updateReply(id: String, isPublic: Boolean) =
        withContext(Dispatchers.IO) {
            webService.updateReply(id, isPublic)
        }

    override suspend fun createMessage(body: String): Message =
        withContext(Dispatchers.IO) {
            val currentMode = preferenceManager.currentMode.value
            val newMessage = webService.createMessage(
                CreateMessageRequest(body, currentMode.apiValue)
            )
            localDao.insertMessage(newMessage.asMessageDB())
            newMessage
        }

    override suspend fun createReply(body: String?, isPublic: Boolean, parentId: String?) =
        withContext(Dispatchers.IO) {
            webService.createReply(body, isPublic, parentId)
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