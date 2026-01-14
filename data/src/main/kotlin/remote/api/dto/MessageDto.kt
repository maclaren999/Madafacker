package remote.api.dto

import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.MessageState

/**
 * Network DTO for Message - matches API response structure
 * 
 * Note: API was updated in 2026. New API structure:
 * - 'authorId' replaced with 'author' object containing {id, name, createdAt}
 * - 'updatedAt' is now optional (only present on outcoming messages)
 * - 'public', 'wasSent', 'parentId' removed (now nullable)
 * - New fields added: 'ratingStats', 'ownRating'
 */
data class MessageDto(
    val id: String,
    val body: String,
    val mode: String,
    // Old API fields (now nullable for backward compatibility)
    val public: Boolean? = null,
    val wasSent: Boolean? = null,
    val updatedAt: String? = null,
    val authorId: String? = null,
    val parentId: String? = null,
    // Required fields
    val createdAt: String,
    // New API fields
    val author: AuthorDto? = null,
    val ratingStats: RatingStatsDto? = null,
    val ownRating: String? = null,
    // Replies (can contain updated structure too)
    val replies: List<ReplyDto>? = null
)

// Extension functions for mapping
fun MessageDto.toDomainModel(): Message {
    // Derive authorId from author object if authorId is not directly available
    val resolvedAuthorId = authorId ?: author?.id
        ?: throw IllegalStateException("Message $id has no authorId and no author object - malformed API response")
    
    return Message(
        id = id,
        body = body,
        mode = mode,
        isPublic = public ?: true,
        createdAt = createdAt,
        // Use createdAt as fallback when updatedAt is not provided
        updatedAt = updatedAt ?: createdAt,
        parentId = parentId,
        authorId = resolvedAuthorId,
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