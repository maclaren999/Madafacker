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
    val isPublic: Boolean,
    val createdAt: String,
    val authorId: String
)

// Extension functions for mapping
fun MessageDto.toDomainModel(): Message {
    return Message(
        id = id,
        body = body,
        mode = mode,
        isPublic = isPublic,
        createdAt = createdAt,
        authorId = authorId,
        // Client-only fields get default values
        localState = MessageState.SENT,
        localCreatedAt = System.currentTimeMillis(),
        tempId = null,
        needsSync = false,
        // New messages from server are unread by default
        isRead = false,
        readAt = null
    )
}

fun Message.toNetworkDto(): MessageDto {
    return MessageDto(
        id = id,
        body = body,
        mode = mode,
        isPublic = isPublic,
        createdAt = createdAt,
        authorId = authorId
        // Client-only fields are excluded
    )
}