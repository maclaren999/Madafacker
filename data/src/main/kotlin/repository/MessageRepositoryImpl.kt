package repository

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.PendingMessage
import com.bbuddies.madafaker.common_domain.model.Reply
import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import com.bbuddies.madafaker.common_domain.repository.MessageRepository
import com.bbuddies.madafaker.common_domain.repository.PendingMessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import local.MadafakerDao
import remote.api.MadafakerApi
import remote.api.request.CreateMessageRequest
import timber.log.Timber
import worker.SendMessageWorker
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val webService: MadafakerApi,
    private val preferenceManager: PreferenceManager,
    private val localDao: MadafakerDao,
    private val workManager: WorkManager,
    private val pendingMessageRepository: PendingMessageRepository
) : MessageRepository {

    companion object {
        private const val SEND_MESSAGE_WORK_PREFIX = "send_message_work"
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
                localDao.insertMessage(newMessage)

                Timber.d("Message sent successfully: ${newMessage.id}")
                newMessage

            } catch (exception: Exception) {
                Timber.w(exception, "Failed to send message immediately, saving as pending")

                // Create pending message
                val pendingMessage = PendingMessage(
                    id = UUID.randomUUID().toString(),
                    body = body,
                    mode = currentMode.apiValue,
                    createdAt = System.currentTimeMillis(),
                    retryCount = 0,
                    lastRetryAt = null
                )

                // Save to pending messages
                pendingMessageRepository.savePendingMessage(pendingMessage)

                // Schedule WorkManager job with exponential backoff
                schedulePendingMessageSend(pendingMessage)

                // Return a temporary message for UI feedback
                Message(
                    id = "temp_${pendingMessage.id}",
                    body = body,
                    mode = currentMode.apiValue,
                    isPublic = true,
                    createdAt = System.currentTimeMillis().toString(),
                    authorId = "temp_author"
                )
            }
        }

    override suspend fun createReply(body: String?, isPublic: Boolean, parentId: String?) =
        withContext(Dispatchers.IO) {
            webService.createReply(body, isPublic, parentId)
        }

    override suspend fun retryPendingMessages() {
        withContext(Dispatchers.IO) {
            val pendingMessages = pendingMessageRepository.getAllPendingMessages()
            Timber.d("Found ${pendingMessages.size} pending messages to retry")

            pendingMessages.forEach { pendingMessage ->
                schedulePendingMessageSend(pendingMessage)
            }
        }
    }

    override suspend fun hasPendingMessages(): Boolean {
        return pendingMessageRepository.hasPendingMessages()
    }

    private fun schedulePendingMessageSend(pendingMessage: PendingMessage) {
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
                    SendMessageWorker.KEY_MESSAGE_BODY to pendingMessage.body,
                    SendMessageWorker.KEY_MESSAGE_MODE to pendingMessage.mode,
                    SendMessageWorker.KEY_PENDING_MESSAGE_ID to pendingMessage.id,
                    SendMessageWorker.KEY_RETRY_COUNT to pendingMessage.retryCount
                )
            )
            .build()

        // Use unique work to avoid duplicates for the same pending message
        val workName = "$SEND_MESSAGE_WORK_PREFIX-${pendingMessage.id}"
        workManager.enqueueUniqueWork(
            workName,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )

        Timber.d("Scheduled pending message send work: ${workRequest.id} for message: ${pendingMessage.id}")
    }
}