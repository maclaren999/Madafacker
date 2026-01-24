package com.bbuddies.madafaker.presentation.ui.main.tabs

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.MessageState
import com.bbuddies.madafaker.common_domain.model.MessageWithReplies
import com.bbuddies.madafaker.common_domain.model.RatingStats
import com.bbuddies.madafaker.common_domain.model.Reply
import com.bbuddies.madafaker.presentation.R
import com.bbuddies.madafaker.presentation.base.HandleState
import com.bbuddies.madafaker.presentation.design.theme.MadafakerTheme
import com.bbuddies.madafaker.presentation.ui.main.MainScreenContract
import com.bbuddies.madafaker.presentation.ui.main.MainTab
import com.bbuddies.madafaker.presentation.ui.main.components.InboxMessage
import com.bbuddies.madafaker.presentation.ui.main.components.MessageCard
import com.bbuddies.madafaker.presentation.ui.main.components.toInboxMessages
import com.bbuddies.madafaker.presentation.ui.main.preview.PreviewMainScreenContract

@Composable
fun InboxTab(
    viewModel: MainScreenContract,
    highlightedMessageId: String? = null
) {
    val incomingMessages by viewModel.incomingMessages.collectAsState()
    val isReplySending by viewModel.isReplySending.collectAsState()
    val replyError by viewModel.replyError.collectAsState()
    val replyingMessageId by viewModel.replyingMessageId.collectAsState()
    val userRepliesForMessage by viewModel.userRepliesForMessage.collectAsState()
    val inboxSnackbarMessage by viewModel.inboxSnackbarMessage.collectAsState()

    val currentUser = if (viewModel is com.bbuddies.madafaker.presentation.ui.main.MainViewModel) {
        viewModel.currentUser.collectAsState().value
    } else null

    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar for inbox-specific messages (reply success, rating, etc.)
    LaunchedEffect(inboxSnackbarMessage) {
        inboxSnackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearInboxSnackbar()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        incomingMessages.HandleState(
            onRetry = viewModel::refreshMessages
        ) { messages ->
            InboxMessageList(
                messages = messages.toInboxMessages(),
                highlightedMessageId = highlightedMessageId,
                replyingMessageId = replyingMessageId,
                userRepliesForMessage = userRepliesForMessage,
                viewModel = viewModel,
                isReplySending = isReplySending,
                replyError = replyError,
                currentUserId = currentUser?.id
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) { snackbarData ->
            Snackbar(
                snackbarData = snackbarData,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun InboxMessageList(
    messages: List<InboxMessage>,
    highlightedMessageId: String? = null,
    replyingMessageId: String? = null,
    userRepliesForMessage: List<Reply>,
    viewModel: MainScreenContract,
    isReplySending: Boolean,
    replyError: String?,
    currentUserId: String? = null
) {
    if (messages.isEmpty()) {
        InboxEmptyState()
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        items(messages) { msg ->
            val isHighlighted = highlightedMessageId == msg.id
            val isReplying = replyingMessageId == msg.id

            val cardContainerModifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .padding(4.dp)

            MessageCard(
                message = msg,
                modifier = cardContainerModifier,
                isReplying = isReplying,
                userReplies = if (isReplying) userRepliesForMessage else emptyList(),
                onMessageTapped = {
                    viewModel.onMessageTapped(msg.id)
                },
                onSendReply = { messageId, replyText, isPublic ->
                    viewModel.onSendReply(messageId, replyText, isPublic)
                },
                onReplyingClosed = {
                    viewModel.onMessageReplyingClosed()
                },
                onRateMessage = { messageId, rating ->
                    viewModel.onRateMessage(messageId, rating)
                },
                isReplySending = isReplySending,
                replyError = replyError,
                currentUserId = currentUserId
            )

            if (isHighlighted) {
                LaunchedEffect(msg.id) {
                    viewModel.markMessageAsRead(msg.id)
                }
            }
        }
    }
}

@Composable
private fun InboxEmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.inbox_empty_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.inbox_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.inbox_empty_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodySmall.lineHeight
            )
        }
    }
}

private val previewInboxReplies = listOf(
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

private val previewInboxMessages = listOf(
    MessageWithReplies(
        message = Message(
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
            readAt = null
        ),
        replies = previewInboxReplies
    ),
    MessageWithReplies(
        message = Message(
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
            readAt = null
        ),
        replies = emptyList()
    ),
    MessageWithReplies(
        message = Message(
            id = "message-3",
            body = "Share a small win you had today.",
            mode = Mode.SHADOW.apiValue,
            createdAt = "2024-02-02T14:00:00Z",
            authorId = "user-2",
            authorName = "User2",
            ratingStats = RatingStats(likes = 8, dislikes = 2, superLikes = 3),
            ownRating = null,
            localState = MessageState.SENT,
            localCreatedAt = System.currentTimeMillis(),
            tempId = null,
            needsSync = false,
            isRead = false,
            readAt = null
        ),
        replies = previewInboxReplies
    )
)

@Preview(showBackground = true)
@Composable
private fun InboxTabPreview() {
    val firstMessage = previewInboxMessages.first()
    MadafakerTheme(mode = Mode.SHINE) {
        InboxTab(
            viewModel = PreviewMainScreenContract(
                draftText = "Staying curious.",
                incomingMessages = previewInboxMessages,
                currentTab = MainTab.INBOX,
                highlightedMessageId = firstMessage.message.id,
                replyingMessageId = firstMessage.message.id,
                userReplies = previewInboxReplies
            ),
            highlightedMessageId = firstMessage.message.id
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InboxTabEmptyPreview() {
    MadafakerTheme(mode = Mode.SHINE) {
        InboxEmptyState()
    }
}

