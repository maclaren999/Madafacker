package local

import com.bbuddies.madafaker.common_domain.model.PendingMessage
import com.bbuddies.madafaker.common_domain.repository.PendingMessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PendingMessageRepositoryImpl @Inject constructor(
    private val dao: MadafakerDao
) : PendingMessageRepository {

    override suspend fun savePendingMessage(pendingMessage: PendingMessage) =
        withContext(Dispatchers.IO) {
            dao.insertPendingMessage(pendingMessage)
        }

    override suspend fun getAllPendingMessages(): List<PendingMessage> =
        withContext(Dispatchers.IO) {
            dao.getAllPendingMessages()
        }

    override suspend fun getPendingMessageById(id: String): PendingMessage? =
        withContext(Dispatchers.IO) {
            dao.getPendingMessageById(id)
        }

    override suspend fun deletePendingMessage(id: String) =
        withContext(Dispatchers.IO) {
            dao.deletePendingMessage(id)
        }

    override suspend fun deleteAllPendingMessages() =
        withContext(Dispatchers.IO) {
            dao.deleteAllPendingMessages()
        }

    override suspend fun updatePendingMessage(pendingMessage: PendingMessage) =
        withContext(Dispatchers.IO) {
            dao.updatePendingMessage(pendingMessage)
        }

    override suspend fun hasPendingMessages(): Boolean =
        withContext(Dispatchers.IO) {
            dao.getPendingMessagesCount() > 0
        }

    override suspend fun incrementRetryCount(id: String) =
        withContext(Dispatchers.IO) {
            val pendingMessage = dao.getPendingMessageById(id)
            if (pendingMessage != null) {
                val updated = pendingMessage.copy(
                    retryCount = pendingMessage.retryCount + 1,
                    lastRetryAt = System.currentTimeMillis()
                )
                dao.updatePendingMessage(updated)
            }
        }
}
