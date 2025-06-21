package repository

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.MessageState
import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import com.bbuddies.madafaker.common_domain.repository.MessageRepository
import com.bbuddies.madafaker.common_domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import local.MadafakerDao
import remote.api.MadafakerApi
import remote.api.request.CreateMessageRequest
import timber.log.Timber
import worker.SendMessageWorker
import java.util.UUID
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val webService: MadafakerApi,
    private val localDao: MadafakerDao,
    private val workManager: WorkManager,
    private val preferenceManager: PreferenceManager,
    private val userRepository: UserRepository
) : MessageRepository {

    // Single source of truth - local database
    override fun observeIncomingMessages(): Flow<List<Message>> {
        return userRepository.currentUser.value?.id?.let {
            localDao.observeIncomingMessages(it)
        } ?: emptyFlow()
    }

    override fun observeOutgoingMessages(): Flow<List<Message>>? {
        return userRepository.currentUser.value?.id?.let {
            localDao.observeOutgoingMessages(it)
        } ?: emptyFlow()
    }

    override suspend fun createMessage(body: String): Message {
        val user = userRepository.currentUser.value ?: throw Exception("No user")

        val tempId = "temp_${UUID.randomUUID()}"
        val currentMode = preferenceManager.currentMode.value

        // Create local message immediately
        val localMessage = Message(
            id = tempId,
            body = body,
            mode = currentMode.apiValue,
            isPublic = true,
            createdAt = System.currentTimeMillis().toString(),
            authorId = user.id,
            localState = MessageState.PENDING,
            tempId = tempId,
            needsSync = true
        )

        localDao.insertMessage(localMessage)

        // Try immediate send
        try {
            val serverMessage = webService.createMessage(
                CreateMessageRequest(body, currentMode.apiValue)
            )

            // Replace temp message with server message
            localDao.deleteMessage(tempId)
            localDao.insertMessage(
                serverMessage.copy(
                    localState = MessageState.SENT,
                    localCreatedAt = System.currentTimeMillis(),
                    needsSync = false
                )
            )

            return serverMessage

        } catch (exception: Exception) {
            // Schedule background send
            schedulePendingMessageSend(localMessage)
            return localMessage
        }
    }

    override suspend fun refreshMessages() {
        try {
            // Fetch from server using existing API
            val serverIncoming = webService.getIncomingMassage()
            val serverOutgoing = webService.getOutcomingMassage()

            // Simple merge: replace all non-pending local messages
            val pendingMessages = localDao.getMessagesByState(MessageState.PENDING)
            val allServerMessages = (serverIncoming + serverOutgoing).map { serverMsg ->
                serverMsg.copy(
                    localState = MessageState.SENT,
                    localCreatedAt = System.currentTimeMillis(),
                    needsSync = false
                )
            }

            // Clear synced messages and insert fresh server data
            localDao.deleteMessagesByState(MessageState.SENT)
            localDao.insertMessages(allServerMessages)

        } catch (e: Exception) {
            Timber.w(e, "Failed to refresh from server")
        }
    }

    override suspend fun retryPendingMessages() {
        val pendingMessages = localDao.getMessagesByState(MessageState.PENDING)
        pendingMessages.forEach { message ->
            schedulePendingMessageSend(message)
        }
    }

    override suspend fun hasPendingMessages(): Boolean {
        return localDao.getMessagesByState(MessageState.PENDING).isNotEmpty()
    }

    private fun schedulePendingMessageSend(message: Message) {
        val workRequest = OneTimeWorkRequestBuilder<SendMessageWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(
                workDataOf(
                    SendMessageWorker.KEY_MESSAGE_BODY to message.body,
                    SendMessageWorker.KEY_MESSAGE_MODE to message.mode,
                    SendMessageWorker.KEY_TEMP_MESSAGE_ID to message.id
                )
            )
            .addTag("send_message")
            .build()

        workManager.enqueueUniqueWork(
            "send_message_${message.id}",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}