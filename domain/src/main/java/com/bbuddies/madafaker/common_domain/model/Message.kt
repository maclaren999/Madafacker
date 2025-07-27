package com.bbuddies.madafaker.common_domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "messages")
data class Message(
    @PrimaryKey val id: String,
    val body: String,
    val mode: String,
    val isPublic: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val authorId: String,
    val parentId: String? = null, // Always null for messages, only exists for replies
    val replies: List<Reply>? = null, // Replies to this message

    // CLIENT-ONLY fields (not sent to/from server)
    val localState: MessageState = MessageState.SENT,
    val localCreatedAt: Long = System.currentTimeMillis(),
    val tempId: String? = null,
    val needsSync: Boolean = false,

    // Read state tracking
    val isRead: Boolean = false,
    val readAt: Long? = null
)

// Separate client-only states
@Serializable
enum class MessageState {
    PENDING,         // Queued for sending
    SENT,            // Confirmed sent
    FAILED           // Failed to send
}