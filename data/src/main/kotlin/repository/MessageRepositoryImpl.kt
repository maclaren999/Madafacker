package repository

import com.bbuddies.madafaker.common_domain.enums.MessageRating
import com.bbuddies.madafaker.common_domain.model.AuthenticationState
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.MessageState
import com.bbuddies.madafaker.common_domain.model.MessageSendException
import com.bbuddies.madafaker.common_domain.model.Reply
import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import com.bbuddies.madafaker.common_domain.repository.MessageRepository
import com.bbuddies.madafaker.common_domain.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import local.MadafakerDao
import remote.api.MadafakerApi
import remote.api.dto.toDomainModel
import remote.api.request.CreateMessageRequest
import remote.api.request.CreateReplyRequest
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val webService: MadafakerApi,
    private val localDao: MadafakerDao,
    private val preferenceManager: PreferenceManager,
    private val userRepository: UserRepository
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

    private val errorJson = Json { ignoreUnknownKeys = true }

    private data class ApiErrorDetails(
        val message: String? = null,
        val code: String? = null
    )

    private fun parseErrorDetails(errorBody: String?): ApiErrorDetails {
        if (errorBody.isNullOrBlank()) return ApiErrorDetails()

        return try {
            val element = errorJson.decodeFromString<JsonElement>(errorBody)
            val obj = element.jsonObject
            val message = obj["message"]?.jsonPrimitive?.contentOrNull
                ?: obj["error"]?.jsonPrimitive?.contentOrNull
                ?: obj["detail"]?.jsonPrimitive?.contentOrNull
                ?: obj["details"]?.jsonPrimitive?.contentOrNull
            val code = obj["code"]?.jsonPrimitive?.contentOrNull
                ?: obj["errorCode"]?.jsonPrimitive?.contentOrNull
                ?: obj["error_code"]?.jsonPrimitive?.contentOrNull

            ApiErrorDetails(message = message, code = code)
        } catch (e: Exception) {
            ApiErrorDetails(message = errorBody.trim())
        }
    }

    private fun mapSendMessageException(exception: Exception): MessageSendException {
        return when (exception) {
            is HttpException -> {
                val statusCode = exception.code()
                val details = parseErrorDetails(exception.response()?.errorBody()?.string())
                val isClientError = statusCode in 400..499

                MessageSendException(
                    statusCode = statusCode,
                    errorCode = if (isClientError) details.code else null,
                    errorMessage = if (isClientError) details.message else null,
                    cause = exception
                )
            }

            else -> MessageSendException(
                errorMessage = null,
                cause = exception
            )
        }
    }

    override suspend fun createMessage(body: String): Message {
        userRepository.awaitCurrentUser()
            ?: throw IllegalStateException("No authenticated user available")

        val currentMode = preferenceManager.currentMode.value
        val trimmedBody = body.trim()

        try {
            val serverMessage = webService.createMessage(
                CreateMessageRequest(trimmedBody, currentMode.apiValue)
            ).toDomainModel()

            localDao.insertMessage(
                serverMessage.copy(
                    localState = MessageState.SENT,
                    localCreatedAt = System.currentTimeMillis(),
                    needsSync = false
                )
            )

            return serverMessage

        } catch (exception: Exception) {
            throw mapSendMessageException(exception)
        }
    }

    override suspend fun refreshMessages() {
        try {
            // Fetch from server using existing API
            val serverIncoming = webService.getIncomingMessages().map { it.toDomainModel() }
            val serverOutgoing = webService.getOutcomingMessages().map { it.toDomainModel() }

            // Replace local messages with the latest server snapshot
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

    override suspend fun refreshIncomingMessages() {
        try {
            // Fetch only incoming messages from server
            val serverIncoming = webService.getIncomingMessages().map { it.toDomainModel() }

            // Get current user to filter existing incoming messages
            val user = userRepository.awaitCurrentUser() ?: return

            // Convert server messages to local format
            val incomingMessages = serverIncoming.map { serverMsg ->
                serverMsg.copy(
                    localState = MessageState.SENT,
                    localCreatedAt = System.currentTimeMillis(),
                    needsSync = false
                )
            }

            // Remove existing incoming messages (not authored by current user) and insert fresh data
            localDao.deleteIncomingMessages(user.id)
            localDao.insertMessages(incomingMessages)

            Timber.d("Refreshed ${incomingMessages.size} incoming messages")
        } catch (e: Exception) {
            Timber.w(e, "Failed to refresh incoming messages from server")
        }
    }

    @Deprecated("Postponed sending removed.")
    override suspend fun hasPendingMessages(): Boolean {
        return false
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

    override suspend fun rateMessage(messageId: String, rating: MessageRating) {
        try {
            // Ensure user is authenticated
            val user = userRepository.awaitCurrentUser()
                ?: throw IllegalStateException("No authenticated user available")

            // Rate message via API
            val request = remote.api.request.RateMessageRequest(rating = rating.apiValue)
            webService.rateMessage(messageId, request)

            // Note: The API doesn't return updated message data, so we don't update local storage
            // The rating is fire-and-forget for now

        } catch (e: Exception) {
            Timber.e(e, "Failed to rate message: $messageId with rating: ${rating.apiValue}")
            throw e
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












