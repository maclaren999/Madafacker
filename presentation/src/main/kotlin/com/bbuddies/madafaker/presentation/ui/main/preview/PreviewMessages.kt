package com.bbuddies.madafaker.presentation.ui.main.preview

import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.MessageState
import com.bbuddies.madafaker.common_domain.model.RatingStats
import com.bbuddies.madafaker.common_domain.model.Reply

object PreviewMessages {
    val sampleReplies = listOf(
        Reply(
            id = "reply-1",
            body = "Appreciate the positive energy here.",
            mode = Mode.SHINE.apiValue,
            createdAt = "2024-02-01T10:00:00Z",
            authorId = "user-reply-1",
            authorName = "ReplyUser1",
            parentMessageId = "message-1"
        ),
        Reply(
            id = "reply-2",
            body = "Thanks for sharing this perspective.",
            mode = Mode.SHADOW.apiValue,
            createdAt = "2024-02-02T09:30:00Z",
            authorId = "user-reply-2",
            authorName = "ReplyUser2",
            parentMessageId = "message-1"
        )
    )

    val sampleOutgoing = listOf(
        Message(
            id = "sent-1",
            body = "Shared a reminder to drink water today.",
            mode = Mode.SHINE.apiValue,
            createdAt = "2024-04-01T08:00:00Z",
            authorId = "preview-user",
            authorName = "Preview User",
            localState = MessageState.SENT
        ),
        Message(
            id = "sent-2",
            body = "Drafting a note about handling tough days.",
            mode = Mode.SHADOW.apiValue,
            createdAt = "2024-04-02T10:00:00Z",
            authorId = "preview-user",
            authorName = "Preview User",
            localState = MessageState.FAILED
        )
    )

    val sampleIncoming = listOf(
        Message(
            id = "message-1",
            body = "What helps you reset after a long week?",
            mode = Mode.SHINE.apiValue,
            createdAt = "2024-02-01T09:00:00Z",
            authorId = "user-1",
            authorName = "User1",
            ratingStats = RatingStats(likes = 5, dislikes = 1, superLikes = 2),
            ownRating = null,
            localState = MessageState.SENT,
            localCreatedAt = System.currentTimeMillis(),
            tempId = null,
            needsSync = false,
            isRead = false,
            readAt = null,
            replies = sampleReplies
        ),
        Message(
            id = "message-2",
            body = "Share a small win you had today.",
            mode = Mode.SHADOW.apiValue,
            createdAt = "2024-02-02T14:00:00Z",
            authorId = "user-2",
            authorName = "User2",
            ratingStats = RatingStats(likes = 3, dislikes = 0, superLikes = 1),
            ownRating = null,
            localState = MessageState.SENT,
            localCreatedAt = System.currentTimeMillis(),
            tempId = null,
            needsSync = false,
            isRead = false,
            readAt = null,
            replies = emptyList()
        )
    )
}
