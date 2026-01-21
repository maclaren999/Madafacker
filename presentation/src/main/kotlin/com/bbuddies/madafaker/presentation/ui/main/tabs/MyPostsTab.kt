package com.bbuddies.madafaker.presentation.ui.main.tabs

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
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
import com.bbuddies.madafaker.presentation.utils.SharedTextManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


@Composable
fun MyPostsTab(viewModel: MainScreenContract) {
    val outcomingMessages by viewModel.outcomingMessages.collectAsState()

    outcomingMessages.HandleState(
        onRetry = viewModel::refreshMessages
    ) { messages ->
        MyPostsList(messages, mode = viewModel.currentMode.value)
    }
}

@Composable
private fun MyPostsList(messages: List<Message>, mode: Mode) {
    if (messages.isEmpty()) {
        EmptyStateView(
            title = stringResource(R.string.no_posts_yet_title),
            subtitle = stringResource(R.string.no_posts_yet_subtitle)
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp),
    ) {
        items(
            items = messages,
            key = { message -> message.id }
        ) { message ->
            MyPostCard(message, mode)
        }
    }
}

@Composable
private fun EmptyStateView(
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
private fun MyPostCard(message: Message, mode: Mode) {
    val replies = message.replies.orEmpty()
    val latestReply = replies.maxByOrNull { reply ->
        reply.createdAt
    }

    MyPostCardLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
        indicator = {
            Image(
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize(),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),
                painter = painterResource(
                    if (mode == Mode.SHINE) R.drawable.untitled_vertical_light
                    else R.drawable.untitled_vertical_dark
                )
            )
        }
    ) {
        Text(
            text = message.body,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            maxLines = 6,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = stringResource(R.string.replies_count, replies.size),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelSmall
        )


        latestReply?.let { reply ->
            LatestReplyHighlight(reply = reply)
        }
    }
}

@Composable
private fun MyPostCardLayout(
    modifier: Modifier = Modifier,
    indicator: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val indicatorWidth = 4.dp
    val spacerWidth = 8.dp

    Layout(
        modifier = modifier,
        content = {
            Box { indicator() }
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                content()
            }
        }
    ) { measurables, constraints ->
        val indicatorWidthPx = indicatorWidth.roundToPx()
        val spacerWidthPx = spacerWidth.roundToPx()
        val contentMaxWidth = (constraints.maxWidth - indicatorWidthPx - spacerWidthPx)
            .coerceAtLeast(0)

        val contentPlaceable = measurables[1].measure(
            constraints.copy(
                minWidth = 0,
                maxWidth = contentMaxWidth
            )
        )

        val indicatorPlaceable = measurables[0].measure(
            Constraints.fixed(
                width = indicatorWidthPx,
                height = contentPlaceable.height
            )
        )

        val layoutWidth = indicatorPlaceable.width + spacerWidthPx + contentPlaceable.width
        val layoutHeight = contentPlaceable.height

        layout(layoutWidth, layoutHeight) {
            indicatorPlaceable.placeRelative(0, 0)
            contentPlaceable.placeRelative(indicatorPlaceable.width + spacerWidthPx, 0)
        }
    }
}


@Composable
private fun LatestReplyHighlight(reply: Reply) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = "@${reply.authorName}",
            style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = reply.body,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(bottom = 6.dp)
        )
    }
}

private val previewRepliesForMyPosts = listOf(
    Reply(
        id = "my-post-1-reply-1",
        body = "Thanks for sharing this - it resonated with me today.",
        mode = Mode.SHINE.apiValue,
        createdAt = "2024-03-01T10:30:00Z",
        authorId = "replying-user-1",
        authorName = "ReplyingUser1",
        parentMessageId = "my-post-1"
    ),
    Reply(
        id = "my-post-1-reply-2",
        body = "Loved this reminder. Keep posting more!",
        mode = Mode.SHINE.apiValue,
        createdAt = "2024-03-01T11:00:00Z",
        authorId = "replying-user-2",
        authorName = "ReplyingUser2",
        parentMessageId = "my-post-1"
    )
)

private val previewMyPosts = listOf(
    Message(
        id = "my-post-1",
        body = "Just shared a thought with the community about staying motivated.",
        mode = Mode.SHINE.apiValue,
        createdAt = "2024-03-01T10:00:00Z",
        authorId = "preview-user",
        authorName = "PreviewUser",
        ratingStats = RatingStats(likes = 5, dislikes = 1, superLikes = 2),
        ownRating = null,
        localState = MessageState.SENT,
        localCreatedAt = System.currentTimeMillis(),
        tempId = null,
        needsSync = false,
        isRead = true,
        readAt = null,
        replies = previewRepliesForMyPosts
    ),
    Message(
        id = "my-post-2",
        body = "Working on a longer story about focus and balance.",
        mode = Mode.SHADOW.apiValue,
        createdAt = "2024-03-02T09:00:00Z",
        authorId = "preview-user",
        authorName = "PreviewUser",
        ratingStats = RatingStats(),
        ownRating = null,
        localState = MessageState.FAILED,
        localCreatedAt = System.currentTimeMillis(),
        tempId = null,
        needsSync = true,
        isRead = true,
        readAt = null,
        replies = emptyList()
    ),
    Message(
        id = "my-post-3",
        body = "Draft failed to send, needs another try.",
        mode = Mode.SHADOW.apiValue,
        createdAt = "2024-03-02T09:00:00Z",
        authorId = "preview-user",
        authorName = "PreviewUser",
        ratingStats = RatingStats(),
        ownRating = null,
        localState = MessageState.FAILED,
        localCreatedAt = System.currentTimeMillis(),
        tempId = null,
        needsSync = true,
        isRead = true,
        readAt = null,
        replies = emptyList()
    )
)
private class PreviewMyPostsContract(
    initialMessages: List<Message> = previewMyPosts
) : MainScreenContract {
    override val draftMessage: StateFlow<String> = MutableStateFlow("")
    override val isSending: StateFlow<Boolean> = MutableStateFlow(false)
    override val sendStatus: StateFlow<SendMessageStatus> = MutableStateFlow(SendMessageStatus.Idle)
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



