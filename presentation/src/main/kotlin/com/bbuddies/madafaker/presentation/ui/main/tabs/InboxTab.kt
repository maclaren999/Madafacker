package com.bbuddies.madafaker.presentation.ui.main.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bbuddies.madafaker.common_domain.model.Reply
import com.bbuddies.madafaker.presentation.R
import com.bbuddies.madafaker.presentation.base.HandleState
import com.bbuddies.madafaker.presentation.ui.main.MainScreenContract

import com.bbuddies.madafaker.presentation.ui.main.components.InboxMessage
import com.bbuddies.madafaker.presentation.ui.main.components.MessageCard
import com.bbuddies.madafaker.presentation.ui.main.components.toInboxMessages

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
            replyError = replyError
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
    replyError: String?
) {
    if (messages.isEmpty()) {
        InboxEmptyState()
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        items(messages) { msg ->
            val isHighlighted = highlightedMessageId == msg.id
            val isReplying = replyingMessageId == msg.id

            MessageCard(
                message = msg,
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
                isReplySending = isReplySending,
                replyError = replyError
            )

            // Mark highlighted message as read when displayed
            if (isHighlighted) {
                LaunchedEffect(msg.id) {
                    viewModel.markMessageAsRead(msg.id)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
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
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Text(
                text = "📬",
                fontSize = 64.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = stringResource(R.string.inbox_empty_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                text = stringResource(R.string.inbox_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )

            Spacer(modifier = Modifier.height(16.dp))

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
