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
import com.bbuddies.madafaker.presentation.R
import com.bbuddies.madafaker.presentation.base.HandleState
import com.bbuddies.madafaker.presentation.ui.main.MainScreenContract
import com.bbuddies.madafaker.presentation.ui.main.MainScreenTheme
import com.bbuddies.madafaker.presentation.ui.main.components.HighlightedMessageCard
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

    incomingMessages.HandleState(
        onRetry = viewModel::refreshMessages
    ) { messages ->
        InboxMessageList(
            messages = messages.toInboxMessages(),
            highlightedMessageId = highlightedMessageId,
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
            if (isHighlighted) {
                HighlightedMessageCard(
                    message = msg,
                    onSendReply = { messageId, replyText, isPublic ->
                        viewModel.onSendReply(messageId, replyText, isPublic)
                    },
                    isReplySending = isReplySending,
                    replyError = replyError
                )

                // Mark highlighted message as read when displayed
                LaunchedEffect(msg.id) {
                    viewModel.markMessageAsRead(msg.id)
                }
            } else {
                MessageCard(msg)
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
                color = MainScreenTheme.TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                text = stringResource(R.string.inbox_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MainScreenTheme.TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.inbox_empty_description),
                style = MaterialTheme.typography.bodySmall,
                color = MainScreenTheme.TextSecondary.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodySmall.lineHeight
            )
        }
    }
}
