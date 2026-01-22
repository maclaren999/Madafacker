package com.bbuddies.madafaker.common_domain.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Rating statistics for a message - matches 2026 API structure.
 */
@Serializable
data class RatingStats(
    val likes: Int = 0,
    val dislikes: Int = 0,
    val superLikes: Int = 0
)

/**
 * Message domain model - matches 2026 API structure.
 * Data layer as optional medium between API and UI.
 */
@Serializable
@Entity(tableName = "messages")
data class Message(
    @PrimaryKey val id: String,
    val body: String,
    val mode: String,
    val createdAt: String,
    // Author info (denormalized from nested author object)
    val authorId: String,
    val authorName: String,
    // Rating stats (embedded for Room)
    @Embedded(prefix = "rating_")
    val ratingStats: RatingStats? = null,
    // Own rating (current user's rating on this message)
    val ownRating: String? = null,
    
    // CLIENT-ONLY fields (not from server)
    val localState: MessageState = MessageState.SENT,
    val localCreatedAt: Long = System.currentTimeMillis(),
    val tempId: String? = null,
    val needsSync: Boolean = false,
    val isRead: Boolean = false,
    val readAt: Long? = null
) {
    // Replies are not stored in the messages table, fetched separately
    // Using @Ignore for Room since this is populated from DTO mapping
    @Ignore
    var replies: List<Reply>? = null
    
    constructor(
        id: String,
        body: String,
        mode: String,
        createdAt: String,
        authorId: String,
        authorName: String,
        ratingStats: RatingStats?,
        ownRating: String?,
        localState: MessageState,
        localCreatedAt: Long,
        tempId: String?,
        needsSync: Boolean,
        isRead: Boolean,
        readAt: Long?,
        replies: List<Reply>?
    ) : this(
        id = id,
        body = body,
        mode = mode,
        createdAt = createdAt,
        authorId = authorId,
        authorName = authorName,
        ratingStats = ratingStats,
        ownRating = ownRating,
        localState = localState,
        localCreatedAt = localCreatedAt,
        tempId = tempId,
        needsSync = needsSync,
        isRead = isRead,
        readAt = readAt
    ) {
        this.replies = replies
    }
}

// Separate client-only states
@Serializable
enum class MessageState {
    @Deprecated("Postponed sending removed; pending state kept for legacy data.")
    PENDING,         // Queued for sending
    SENT,            // Confirmed sent
    FAILED           // Failed to send
}
