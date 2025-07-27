package remote.api.dto

import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.MessageState

/**
 * Network DTO for Message - only contains server fields
 */
data class MessageDto(
    val id: String,
    val body: String,
    val mode: String,
    val public: Boolean,
    val wasSent: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val authorId: String,
    val parentId: String? = null,
    val replies: List<ReplyDto>? = null
)

// Extension functions for mapping
fun MessageDto.toDomainModel(): Message {
    return Message(
        id = id,
        body = body,
        mode = mode,
        isPublic = public,
        createdAt = createdAt,
        updatedAt = updatedAt,
        parentId = parentId,
        authorId = authorId,
        // Client-only fields get default values
        localState = MessageState.SENT,
        localCreatedAt = System.currentTimeMillis(),
        tempId = null,
        needsSync = false,
        // New messages from server are unread by default
        isRead = false,
        readAt = null,
        replies = replies?.map { it.toDomainModel() }
    )
}