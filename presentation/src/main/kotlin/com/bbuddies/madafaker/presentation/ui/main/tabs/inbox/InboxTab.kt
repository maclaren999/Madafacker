package com.bbuddies.madafaker.presentation.ui.main.tabs.inbox

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bbuddies.madafaker.common_domain.enums.MessageRating
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.Reply
import com.bbuddies.madafaker.presentation.R
import com.bbuddies.madafaker.presentation.base.HandleState
import com.bbuddies.madafaker.presentation.design.theme.MadafakerTheme
import com.bbuddies.madafaker.presentation.ui.main.components.InboxMessage
import com.bbuddies.madafaker.presentation.ui.main.components.MessageCard
import com.bbuddies.madafaker.presentation.ui.main.components.toInboxMessages
import com.bbuddies.madafaker.presentation.ui.main.preview.PreviewInboxTabContract
import com.bbuddies.madafaker.presentation.ui.main.preview.PreviewMessages

@Composable
fun InboxTab(
    state: InboxTabState,
    highlightedMessageId: String? = null,
    onInboxViewed: () -> Unit,
    onRefreshMessages: () -> Unit,
    onSendReply: (String, String, Boolean) -> Unit,
    onReplyingClosed: () -> Unit,
    onRateMessage: (String, MessageRating) -> Unit,
    onMessageTapped: (String) -> Unit,
    onMarkMessageRead: (String) -> Unit,
    onSnackbarConsumed: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val highlightId = state.highlightedMessageId ?: highlightedMessageId

    LaunchedEffect(Unit) {
        onInboxViewed()
    }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onSnackbarConsumed()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        state.incomingMessages.HandleState(
            onRetry = onRefreshMessages
        ) { messages ->
            InboxMessageList(
                messages = messages.toInboxMessages(),
                highlightedMessageId = highlightId,
                replyingMessageId = state.replyingMessageId,
                userRepliesForMessage = state.userRepliesForMessage,
                onMessageTapped = onMessageTapped,
                onSendReply = onSendReply,
                onReplyingClosed = onReplyingClosed,
                onRateMessage = onRateMessage,
                isReplySending = state.isReplySending,
                replyError = state.replyError,
                currentUserId = state.currentUserId,
                onMarkMessageRead = onMarkMessageRead
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
    onMessageTapped: (String) -> Unit,
    onSendReply: (String, String, Boolean) -> Unit,
    onReplyingClosed: () -> Unit,
    onRateMessage: (String, MessageRating) -> Unit,
    isReplySending: Boolean,
    replyError: String?,
    currentUserId: String? = null,
    onMarkMessageRead: (String) -> Unit
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
                    onMessageTapped(msg.id)
                },
                onSendReply = { messageId, replyText, isPublic ->
                    onSendReply(messageId, replyText, isPublic)
                },
                onReplyingClosed = {
                    onReplyingClosed()
                },
                onRateMessage = { messageId, rating ->
                    onRateMessage(messageId, rating)
                },
                isReplySending = isReplySending,
                replyError = replyError,
                currentUserId = currentUserId
            )

            if (isHighlighted) {
                LaunchedEffect(msg.id) {
                    onMarkMessageRead(msg.id)
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

@Preview(showBackground = true)
@Composable
private fun InboxTabPreview() {
    val firstMessage = PreviewMessages.sampleIncoming.first()
    MadafakerTheme(mode = firstMessage.message.mode.let { Mode.fromApiValue(it) }) {
        InboxTab(
            state = PreviewInboxTabContract(
                incomingMessages = PreviewMessages.sampleIncoming,
                highlightedMessageId = firstMessage.message.id,
                replyingMessageId = firstMessage.message.id,
                userReplies = PreviewMessages.sampleReplies
            ).state.value,
            highlightedMessageId = firstMessage.message.id,
            onInboxViewed = {},
            onRefreshMessages = {},
            onSendReply = { _, _, _ -> },
            onReplyingClosed = {},
            onRateMessage = { _, _ -> },
            onMessageTapped = {},
            onMarkMessageRead = {},
            onSnackbarConsumed = {}
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
