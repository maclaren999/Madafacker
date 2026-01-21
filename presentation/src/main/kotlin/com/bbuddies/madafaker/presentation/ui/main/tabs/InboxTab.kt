package com.bbuddies.madafaker.presentation.ui.main.tabs

import android.content.Context
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bbuddies.madafaker.common_domain.enums.MessageRating
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.MessageState
import com.bbuddies.madafaker.common_domain.model.RatingStats
import com.bbuddies.madafaker.common_domain.model.Reply
import com.bbuddies.madafaker.presentation.R
import com.bbuddies.madafaker.presentation.base.HandleState
import com.bbuddies.madafaker.presentation.base.UiState
import com.bbuddies.madafaker.presentation.design.theme.MadafakerTheme
import com.bbuddies.madafaker.presentation.ui.main.MainScreenContract
import com.bbuddies.madafaker.presentation.ui.main.MainTab
import com.bbuddies.madafaker.presentation.ui.main.SendMessageStatus
import com.bbuddies.madafaker.presentation.ui.main.components.InboxMessage
import com.bbuddies.madafaker.presentation.ui.main.components.MessageCard
import com.bbuddies.madafaker.presentation.ui.main.components.toInboxMessages
import com.bbuddies.madafaker.presentation.utils.SharedTextManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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

    val currentUser = if (viewModel is com.bbuddies.madafaker.presentation.ui.main.MainViewModel) {
        viewModel.currentUser.collectAsState().value
    } else null

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

@Composable
private fun ReplySummaryRow(count: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = stringResource(R.string.replies_count, count),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
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
        replies = previewInboxReplies
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
    ),
    Message(
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
        readAt = null,
        replies = previewInboxReplies
    )
)

private class PreviewInboxContract(
    initialMessages: List<Message> = previewInboxMessages
) : MainScreenContract {
    override val draftMessage: StateFlow<String> = MutableStateFlow("Staying curious.")
    override val isSending: StateFlow<Boolean> = MutableStateFlow(false)
    override val sendStatus: StateFlow<SendMessageStatus> = MutableStateFlow(SendMessageStatus.Idle)
    override val incomingMessages: StateFlow<UiState<List<Message>>> =
        MutableStateFlow(UiState.Success(initialMessages))
    override val outcomingMessages: StateFlow<UiState<List<Message>>> =
        MutableStateFlow(UiState.Success(emptyList()))
    override val currentMode: StateFlow<Mode> = MutableStateFlow(Mode.SHINE)
    override val currentTab: StateFlow<MainTab> = MutableStateFlow(MainTab.INBOX)
    override val isReplySending: StateFlow<Boolean> = MutableStateFlow(false)
    override val replyError: StateFlow<String?> = MutableStateFlow(null)
    override val highlightedMessageId: StateFlow<String?> = MutableStateFlow(initialMessages.firstOrNull()?.id)
    override val replyingMessageId: StateFlow<String?> = MutableStateFlow(initialMessages.firstOrNull()?.id)
    override val userRepliesForMessage: StateFlow<List<Reply>> = MutableStateFlow(previewInboxReplies)
    override val warningsFlow: StateFlow<((Context) -> String?)?> = MutableStateFlow(null)
    override val sharedTextManager: SharedTextManager = SharedTextManager()

    override fun onSendMessage(message: String) = Unit
    override fun onDraftMessageChanged(message: String) = Unit
    override fun toggleMode() = Unit
    override fun refreshMessages() = Unit
    override fun refreshUserData() = Unit
    override fun clearDraft() = Unit
    override fun selectTab(tab: MainTab) = Unit
    override fun onSendReply(messageId: String, replyText: String, isPublic: Boolean) = Unit
    override fun clearReplyError() = Unit
    override fun onRateMessage(messageId: String, rating: MessageRating) = Unit
    override fun onInboxViewed() = Unit
    override fun markMessageAsRead(messageId: String) = Unit
    override fun onMessageTapped(messageId: String) = Unit
    override fun onMessageReplyingClosed() = Unit
}

@Preview(showBackground = true)
@Composable
private fun InboxTabPreview() {
    MadafakerTheme(mode = Mode.SHINE) {
        InboxTab(
            viewModel = PreviewInboxContract(),
            highlightedMessageId = previewInboxMessages.first().id
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



