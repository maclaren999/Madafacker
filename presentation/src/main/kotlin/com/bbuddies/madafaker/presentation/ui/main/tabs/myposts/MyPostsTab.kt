package com.bbuddies.madafaker.presentation.ui.main.tabs.myposts

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
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.MessageWithReplies
import com.bbuddies.madafaker.common_domain.model.Reply
import com.bbuddies.madafaker.presentation.R
import com.bbuddies.madafaker.presentation.base.HandleState
import com.bbuddies.madafaker.presentation.design.theme.MadafakerTheme
import com.bbuddies.madafaker.presentation.ui.main.preview.PreviewMessages
import com.bbuddies.madafaker.presentation.ui.main.preview.PreviewMyPostsTabContract

@Composable
fun MyPostsTab(
    state: MyPostsTabState,
    onRefreshMessages: () -> Unit
) {
    state.outcomingMessages.HandleState(
        onRetry = onRefreshMessages
    ) { messages ->
        MyPostsList(messages, mode = state.currentMode)
    }
}

@Composable
private fun MyPostsList(messagesWithReplies: List<MessageWithReplies>, mode: Mode) {
    if (messagesWithReplies.isEmpty()) {
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
            items = messagesWithReplies,
            key = { item -> item.message.id }
        ) { item ->
            MyPostCard(item.message, mode)
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

        val layoutWidth = indicatorWidthPx + spacerWidthPx + contentPlaceable.width
        val layoutHeight = contentPlaceable.height

        layout(layoutWidth, layoutHeight) {
            indicatorPlaceable.placeRelative(0, 0)
            contentPlaceable.placeRelative(indicatorWidthPx + spacerWidthPx, 0)
        }
    }
}

@Composable
private fun LatestReplyHighlight(reply: Reply) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        Text(
            text = "Latest reply",
            style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Text(
            text = "\"${reply.body}\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MyPostsTabPreview() {
    MadafakerTheme(mode = Mode.SHINE) {
        MyPostsTab(
            state = PreviewMyPostsTabContract(
                outgoingMessages = PreviewMessages.sampleOutgoing
            ).state.value,
            onRefreshMessages = {}
        )
    }
}
