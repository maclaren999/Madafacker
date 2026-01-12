package com.bbuddies.madafaker.presentation.ui.main.tabs

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.bbuddies.madafaker.common_domain.enums.MessageRating
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.MessageState
import com.bbuddies.madafaker.common_domain.model.Reply
import com.bbuddies.madafaker.presentation.R
import com.bbuddies.madafaker.presentation.base.HandleState
import com.bbuddies.madafaker.presentation.base.UiState
import com.bbuddies.madafaker.presentation.design.theme.MadafakerTheme
import com.bbuddies.madafaker.presentation.ui.main.MainScreenContract
import com.bbuddies.madafaker.presentation.ui.main.MainTab
import com.bbuddies.madafaker.presentation.utils.SharedTextManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


@Composable
fun MyPostsTab(viewModel: MainScreenContract) {
    val outcomingMessages by viewModel.outcomingMessages.collectAsState()

    outcomingMessages.HandleState(
        onRetry = viewModel::refreshMessages
    ) { messages ->
        MyPostsList(messages)
    }
}

@Composable
private fun MyPostsList(messages: List<Message>) {
    if (messages.isEmpty()) {
        EmptyStateView(
            emoji = "💬",
            title = stringResource(R.string.no_posts_yet_title),
            subtitle = stringResource(R.string.no_posts_yet_subtitle)
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        items(messages) { message ->
            MyPostCard(message)
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun EmptyStateView(
    emoji: String,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = emoji,
                fontSize = 48.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@Composable
private fun MyPostCard(message: Message) {
    val replies = message.replies.orEmpty()
    val latestReply = replies.maxByOrNull { reply ->
        reply.updatedAt.ifBlank { reply.createdAt }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawWithContent {
                drawContent()
                drawRect(color = Color.Black.copy(alpha = 0.04f))
            }
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
        )

        Column(
            modifier = Modifier
                .padding(16.dp)
                .weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.my_message),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelMedium,
                )

                MessageStateIndicator(messageState = message.localState)
            }

            Text(
                text = message.body,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                maxLines = 6,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.replies_count, replies.size),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            latestReply?.let { reply ->
                Spacer(modifier = Modifier.height(10.dp))
                LatestReplyHighlight(reply = reply)
            }
        }
    }
}

@Composable
fun MessageStateIndicator(messageState: MessageState) {
    val (icon, tint, contentDescription) = when (messageState) {
        MessageState.PENDING -> Triple(Icons.Outlined.Done, Color(0xFFFF9800), stringResource(R.string.message_sending))
        MessageState.SENT -> Triple(Icons.Outlined.CheckCircle, Color(0xFF4CAF50), stringResource(R.string.message_delivered))
        MessageState.FAILED -> Triple(Icons.Outlined.Close, Color(0xFFE53935), stringResource(R.string.message_failed))
    }

    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = tint,
        modifier = Modifier.size(18.dp)
    )
}

@Composable
private fun Reaction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    tint: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = count.toString(),
            color = tint,
            fontSize = MaterialTheme.typography.labelSmall.fontSize
        )
    }
}

@Composable
private fun LatestReplyHighlight(reply: Reply) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(12.dp)
    ) {
        Text(
            text = stringResource(R.string.latest_reply_title),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = reply.body,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = stringResource(
                R.string.latest_reply_from,
                "user_${reply.authorId.take(8)}"
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

private val previewRepliesForMyPosts = listOf(
    Reply(
        id = "my-post-1-reply-1",
        body = "Thanks for sharing this - it resonated with me today.",
        mode = Mode.SHINE.apiValue,
        isPublic = true,
        createdAt = "2024-03-01T10:30:00Z",
        updatedAt = "2024-03-01T10:31:00Z",
        authorId = "replying-user-1",
        parentId = "my-post-1"
    ),
    Reply(
        id = "my-post-1-reply-2",
        body = "Loved this reminder. Keep posting more!",
        mode = Mode.SHINE.apiValue,
        isPublic = true,
        createdAt = "2024-03-01T11:00:00Z",
        updatedAt = "2024-03-01T11:02:00Z",
        authorId = "replying-user-2",
        parentId = "my-post-1"
    )
)

private val previewMyPosts = listOf(
    Message(
        id = "my-post-1",
        body = "Just shared a thought with the community about staying motivated.",
        mode = Mode.SHINE.apiValue,
        isPublic = true,
        createdAt = "2024-03-01T10:00:00Z",
        updatedAt = "2024-03-01T10:05:00Z",
        authorId = "preview-user",
        replies = previewRepliesForMyPosts,
        localState = MessageState.SENT
    ),
    Message(
        id = "my-post-2",
        body = "Working on a longer story about focus and balance.",
        mode = Mode.SHADOW.apiValue,
        isPublic = true,
        createdAt = "2024-03-02T09:00:00Z",
        updatedAt = "2024-03-02T09:10:00Z",
        authorId = "preview-user",
        replies = emptyList(),
        localState = MessageState.PENDING
    )
)

private class PreviewMyPostsContract(
    initialMessages: List<Message> = previewMyPosts
) : MainScreenContract {
    override val draftMessage: StateFlow<String> = MutableStateFlow("")
    override val isSending: StateFlow<Boolean> = MutableStateFlow(false)
    override val incomingMessages: StateFlow<UiState<List<Message>>> =
        MutableStateFlow(UiState.Success(emptyList()))
    override val outcomingMessages: StateFlow<UiState<List<Message>>> =
        MutableStateFlow(UiState.Success(initialMessages))
    override val currentMode: StateFlow<Mode> = MutableStateFlow(Mode.SHINE)
    override val currentTab: StateFlow<MainTab> = MutableStateFlow(MainTab.MY_POSTS)
    override val isReplySending: StateFlow<Boolean> = MutableStateFlow(false)
    override val replyError: StateFlow<String?> = MutableStateFlow(null)
    override val highlightedMessageId: StateFlow<String?> = MutableStateFlow(null)
    override val replyingMessageId: StateFlow<String?> = MutableStateFlow(null)
    override val userRepliesForMessage: StateFlow<List<Reply>> = MutableStateFlow(emptyList())
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
private fun MyPostsTabPreview() {
    MadafakerTheme(mode = Mode.SHINE) {
        MyPostsTab(viewModel = PreviewMyPostsContract())
    }
}
