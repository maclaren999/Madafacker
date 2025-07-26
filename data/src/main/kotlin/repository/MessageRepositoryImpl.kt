package repository

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.bbuddies.madafaker.common_domain.exception.ModerationException
import com.bbuddies.madafaker.common_domain.model.AuthenticationState
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.MessageState
import com.bbuddies.madafaker.common_domain.model.Reply
import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import com.bbuddies.madafaker.common_domain.repository.MessageRepository
import com.bbuddies.madafaker.common_domain.repository.UserRepository
import com.bbuddies.madafaker.common_domain.service.ContentFilterService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import local.MadafakerDao
import remote.api.MadafakerApi
import remote.api.dto.toDomainModel
import remote.api.request.CreateMessageRequest
import remote.api.request.CreateReplyRequest
import retrofit2.HttpException
import timber.log.Timber
import worker.SendMessageWorker
import java.util.UUID
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val webService: MadafakerApi,
    private val localDao: MadafakerDao,
    private val workManager: WorkManager,
    private val preferenceManager: PreferenceManager,
    private val userRepository: UserRepository,
    private val contentFilterService: ContentFilterService
) : MessageRepository {

    // Single source of truth - local database
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeIncomingMessages(): Flow<List<Message>> {
        return userRepository.authenticationState
            .flatMapLatest { authState ->
                when (authState) {
                    is AuthenticationState.Authenticated -> {
                        localDao.observeIncomingMessages(authState.user.id)
                    }

                    else -> emptyFlow()
                }
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeOutgoingMessages(): Flow<List<Message>> {
        return userRepository.authenticationState
            .flatMapLatest { authState ->
                when (authState) {
                    is AuthenticationState.Authenticated -> {
                        localDao.observeOutgoingMessages(authState.user.id)
                    }

                    else -> emptyFlow()
                }
            }
    }

    override suspend fun createMessage(body: String): Message {
        // Use the new await function to get user safely
        val user = userRepository.awaitCurrentUser()
            ?: throw IllegalStateException("No authenticated user available")

        val tempId = "temp_${UUID.randomUUID()}"
        val currentMode = preferenceManager.currentMode.value

        // Perform client-side content filtering for SHINE mode
        val filterResult = contentFilterService.filterContent(body, currentMode)
        if (!filterResult.isAllowed) {
            throw ModerationException.ClientSideViolation(
                violationType = filterResult.violationType!!,
                detectedWords = filterResult.detectedWords,
                mode = currentMode,
                message = filterResult.suggestion ?: "Content violates community guidelines"
            )
        }

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
            ).toDomainModel()

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
            // Handle server-side moderation errors
            if (exception is HttpException && exception.code() == 422) {
                // Parse moderation error response
                val errorBody = exception.response()?.errorBody()?.string()
                if (errorBody != null) {
                    try {
                        // Parse the moderation error (simplified - would need proper JSON parsing)
                        localDao.deleteMessage(tempId) // Remove temp message
                        throw ModerationException.ServerSideViolation(
                            violationType = null, // Would parse from errorBody
                            mode = currentMode,
                            serverMessage = "Content violates community guidelines",
                            suggestion = if (currentMode.apiValue == "light")
                                "Try switching to Shadow mode for uncensored expression"
                            else "This content cannot be shared"
                        )
                    } catch (parseException: Exception) {
                        Timber.w(parseException, "Failed to parse moderation error")
                    }
                }
            }

            // For other errors, schedule background send
            schedulePendingMessageSend(localMessage)
            return localMessage
        }
    }

    override suspend fun refreshMessages() {
        try {
            // Fetch from server using existing API
            val serverIncoming = webService.getIncomingMessages().map { it.toDomainModel() }
            val serverOutgoing = webService.getOutcomingMessages().map { it.toDomainModel() }

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

//    override suspend fun retryPendingMessages() {
//        val pendingMessages = localDao.getMessagesByState(MessageState.PENDING)
//        pendingMessages.forEach { message ->
//            schedulePendingMessageSend(message)
//        }
//    }

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
            .addTag("send_message_with_id_${message.id}")
            .build()

        workManager.enqueueUniqueWork(
            "send_message_${message.id}",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    override suspend fun createReply(body: String, parentId: String, isPublic: Boolean): Reply {
        // Ensure user is authenticated
        val user = userRepository.awaitCurrentUser()
            ?: throw IllegalStateException("No authenticated user available")

        try {
            // Create reply via API
            val request = CreateReplyRequest(
                body = body,
                public = isPublic,
                parentId = parentId
            )

            val replyDto = webService.createReply(request)
            val reply = replyDto.toDomainModel()

            // Store locally
            localDao.insertReply(reply)

            return reply

        } catch (e: Exception) {
            Timber.e(e, "Failed to create reply")
            throw e
        }
    }

    override suspend fun getReplyById(id: String): Reply? {
        return try {
            // Try local first
            val localReply = localDao.getReplyById(id)
            if (localReply != null) {
                return localReply
            }

            // Fallback to API
            val replyDto = webService.getReplyById(id)
            val reply = replyDto.toDomainModel()

            // Cache locally
            localDao.insertReply(reply)

            reply
        } catch (e: Exception) {
            Timber.e(e, "Failed to get reply: $id")
            null
        }
    }

    override suspend fun getRepliesByParentId(parentId: String): List<Reply> {
        return try {
            // For now, return from local database
            // In a full implementation, you might want to sync with server
            localDao.getRepliesByParentId(parentId)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get replies for parent: $parentId")
            emptyList()
        }
    }

    override suspend fun getUserRepliesForMessage(parentId: String): List<Reply> {
        return try {
            // Get current user
            val user = userRepository.awaitCurrentUser()
                ?: return emptyList()

            // Get user's replies for this message
            localDao.getRepliesByParentIdAndAuthor(parentId, user.id)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get user replies for message: $parentId")
            emptyList()
        }
    }

    override suspend fun getMostRecentUnreadMessage(): Message? {
        return try {
            val user = userRepository.awaitCurrentUser() ?: return null
            localDao.getMostRecentUnreadMessage(user.id)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get most recent unread message")
            null
        }
    }

    override suspend fun markMessageAsRead(messageId: String) {
        try {
            localDao.markMessageAsRead(messageId, System.currentTimeMillis())
        } catch (e: Exception) {
            Timber.e(e, "Failed to mark message as read: $messageId")
        }
    }

    override suspend fun markAllIncomingMessagesAsRead() {
        try {
            val user = userRepository.awaitCurrentUser() ?: return
            localDao.markAllIncomingMessagesAsRead(user.id, System.currentTimeMillis())
        } catch (e: Exception) {
            Timber.e(e, "Failed to mark all incoming messages as read")
        }
    }
}