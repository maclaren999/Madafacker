package com.bbuddies.madafaker.presentation.ui.main.tabs

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.MessageState
import com.bbuddies.madafaker.presentation.R
import com.bbuddies.madafaker.presentation.base.HandleState
import com.bbuddies.madafaker.presentation.ui.main.MainScreenContract


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

            // Only show reactions and replies for SENT messages
            if (message.localState == MessageState.SENT) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Mock reactions - these would come from the server in a real app
                    Reaction(Icons.Outlined.ThumbUp, 0, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                    Reaction(
                        Icons.Outlined.KeyboardArrowDown,
                        0,
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    Reaction(Icons.Outlined.FavoriteBorder, 0, Color.Red)

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = stringResource(R.string.replies_count, 0),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
fun MessageStateIndicator(messageState: MessageState) {
    val (text, color) = when (messageState) {
        MessageState.PENDING -> stringResource(R.string.message_sending) to Color(0xFFFF9800)
        MessageState.SENT -> stringResource(R.string.message_delivered) to Color(0xFF4CAF50)
        MessageState.FAILED -> stringResource(R.string.message_failed) to Color(0xFFE53935)
    }

    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Medium
        )
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