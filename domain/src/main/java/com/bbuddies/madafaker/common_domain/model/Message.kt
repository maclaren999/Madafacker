package com.bbuddies.madafaker.common_domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey val id: String,
    val body: String,
    val mode: String,
    val isPublic: Boolean,
    val createdAt: String,
    val authorId: String,

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
enum class MessageState {
    PENDING,         // Queued for sending
    SENT,            // Confirmed sent
    FAILED           // Failed to send
}