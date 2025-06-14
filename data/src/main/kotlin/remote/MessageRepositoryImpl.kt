package remote

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
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
import timber.log.Timber
import worker.SendMessageWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val webService: MadafakerApi,
    private val preferenceManager: PreferenceManager,
    private val localDao: MadafakerDao,
    private val workManager: WorkManager
) : MessageRepository {

    companion object {
        private const val SEND_MESSAGE_WORK_NAME = "send_message_work"
    }

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

            try {
                // First, try to send immediately
                val newMessage = webService.createMessage(
                    CreateMessageRequest(body, currentMode.apiValue)
                )
                localDao.insertMessage(newMessage.asMessageDB())

                Timber.d("Message sent successfully: ${newMessage.id}")
                newMessage

            } catch (exception: Exception) {
                Timber.w(exception, "Failed to send message immediately, scheduling for retry")

                // Save draft for offline retry
                preferenceManager.saveUnsentDraft(body, currentMode.apiValue)

                // Schedule WorkManager job with exponential backoff
                scheduleMessageSend(body, currentMode.apiValue)

                // Return a temporary message for UI feedback
                // This will be replaced when the actual message is sent
                Message(
                    id = "temp_${System.currentTimeMillis()}",
                    body = body,
                    mode = currentMode.apiValue,
                    isPublic = true,
                    createdAt = System.currentTimeMillis().toString(),
                    authorId = "temp_author"
                )
            }
        }

    private fun scheduleMessageSend(body: String, mode: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<SendMessageWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                15, // Initial delay: 15 seconds
                TimeUnit.SECONDS
            )
            .setInputData(
                workDataOf(
                    SendMessageWorker.KEY_MESSAGE_BODY to body,
                    SendMessageWorker.KEY_MESSAGE_MODE to mode
                )
            )
            .build()

        // Use unique work to avoid duplicates
        workManager.enqueueUniqueWork(
            SEND_MESSAGE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )

        Timber.d("Scheduled message send work: ${workRequest.id}")
    }

    override suspend fun createReply(body: String?, isPublic: Boolean, parentId: String?) =
        withContext(Dispatchers.IO) {
            webService.createReply(body, isPublic, parentId)
        }

    /**
     * Retry sending any unsent drafts
     * Called when network becomes available or on app startup
     */
    override suspend fun retryUnsentMessages() {
        withContext(Dispatchers.IO) {
            val unsentDraft = preferenceManager.getUnsentDraft()
            if (unsentDraft != null) {
                Timber.d("Found unsent draft, scheduling retry")
                scheduleMessageSend(unsentDraft.body, unsentDraft.mode)
            }
        }
    }

    /**
     * Check if there are any pending messages to send
     */
    override suspend fun hasPendingMessages(): Boolean {
        return preferenceManager.hasUnsentDraft()
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