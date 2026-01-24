package com.bbuddies.madafaker.common_domain.model

/**
 * Domain model that represents a message with its replies.
 * This is an immutable wrapper that avoids mutating the Message entity.
 */
data class MessageWithReplies(
    val message: Message,
    val replies: List<Reply> = emptyList()
)
