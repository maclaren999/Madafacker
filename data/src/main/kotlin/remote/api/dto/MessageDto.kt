package remote.api.dto

import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.MessageState
import com.bbuddies.madafaker.common_domain.model.RatingStats

/**
 * Network DTO for Message - matches 2026 API structure exactly.
 * Data layer as optional medium between API and UI.
 */
data class MessageDto(
    val id: String,
    val body: String,
    val author: AuthorDto,
    val mode: String,
    val createdAt: String,
    val ratingStats: RatingStats? = null,
    val ownRating: String? = null,
    val replies: List<ReplyDto>? = null
)

/**
 * Maps MessageDto to domain model.
 */
fun MessageDto.toDomainModel(): Message {
    return Message(
        id = id,
        body = body,
        mode = mode,
        createdAt = createdAt,
        authorId = author.id,
        authorName = author.name,
        ratingStats = ratingStats,
        ownRating = ownRating,
        // Client-only fields get default values
        localState = MessageState.SENT,
        localCreatedAt = System.currentTimeMillis(),
        tempId = null,
        needsSync = false,
        isRead = false,
        readAt = null,
        replies = replies?.map { it.toDomainModel(parentMessageId = id) }
    )
}