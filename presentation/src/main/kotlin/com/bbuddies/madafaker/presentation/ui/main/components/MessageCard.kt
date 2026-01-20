package com.bbuddies.madafaker.presentation.ui.main.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bbuddies.madafaker.common_domain.enums.MessageRating
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.RatingStats
import com.bbuddies.madafaker.common_domain.model.Reply
import com.bbuddies.madafaker.presentation.design.components.MadafakerTextField
import com.bbuddies.madafaker.presentation.design.theme.MadafakerTheme


@Composable
fun MessageCard(
    message: InboxMessage,
    modifier: Modifier = Modifier,
    isReplying: Boolean = false,
    userReplies: List<Reply> = emptyList(),
    onMessageTapped: (() -> Unit)? = null,
    onSendReply: ((messageId: String, replyText: String, isPublic: Boolean) -> Unit)? = null,
    onReplyingClosed: (() -> Unit)? = null,
    onRateMessage: ((messageId: String, rating: MessageRating) -> Unit)? = null,
    isReplySending: Boolean = false,
    replyError: String? = null,
    currentUserId: String? = null
) {
    var replyText by remember { mutableStateOf("") }

    val mode = Mode.fromApiValue(message.mode)
    val accentColor = mode.accentColor()
    val replies = userReplies.takeIf { it.isNotEmpty() } ?: message.replies.orEmpty()

    if (isReplying) {
        ReplyingMessageCard(
            message = message,
            replies = replies,
            accentColor = accentColor,
            modifier = modifier,
            replyText = replyText,
            onReplyTextChange = { replyText = it },
            onSendReply = onSendReply,
            onReplyingClosed = onReplyingClosed,
            isReplySending = isReplySending,
            replyError = replyError,
            currentUserId = currentUserId
        )
    } else {
        CollapsedMessageCard(
            message = message,
            replies = replies,
            accentColor = accentColor,
            modifier = modifier,
            onMessageTapped = onMessageTapped,
            currentUserId = currentUserId
        )
    }
}

@Composable
private fun ReplyingMessageCard(
    message: InboxMessage,
    replies: List<Reply>,
    accentColor: Color,
    modifier: Modifier = Modifier,
    replyText: String,
    onReplyTextChange: (String) -> Unit,
    onSendReply: ((messageId: String, replyText: String, isPublic: Boolean) -> Unit)?,
    onReplyingClosed: (() -> Unit)?,
    isReplySending: Boolean,
    replyError: String?,
    currentUserId: String?
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            MessageAuthor(author = message.author)
            MessageBody(
                body = message.body,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            RepliesSection(
                replies = replies,
                currentUserId = currentUserId
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Reply to this message:",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )

            MadafakerTextField(
                value = replyText,
                onValueChange = onReplyTextChange,
                singleLine = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val canSend = replyText.isNotBlank() && !isReplySending && onSendReply != null
                Button(
                    onClick = {
                        onSendReply?.invoke(message.id, replyText.trim(), true)
                        onReplyTextChange("")
                    },
                    enabled = canSend,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor
                    )
                ) {
                    if (isReplySending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Send Reply",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedButton(
                    onClick = { onReplyingClosed?.invoke() },
                    modifier = Modifier.weight(0.3f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Reply",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            replyError?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun CollapsedMessageCard(
    message: InboxMessage,
    replies: List<Reply>,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onMessageTapped: (() -> Unit)?,
    currentUserId: String?
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = onMessageTapped != null) { onMessageTapped?.invoke() }
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(accentColor)
        )

        Column(
            modifier = Modifier
                .padding(16.dp)
                .weight(1f)
        ) {
            MessageAuthor(author = message.author)
            MessageBody(
                body = message.body,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                maxLines = 6,
                overflow = TextOverflow.Ellipsis
            )

            RepliesSection(
                replies = replies,
                currentUserId = currentUserId,
                maxVisible = 3
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                message.up?.let {
                    Reaction(
                        Icons.Outlined.ThumbUp,
                        it,
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
                message.down?.let {
                    Reaction(
                        Icons.Outlined.KeyboardArrowDown,
                        it,
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
                message.hearts?.let {
                    Reaction(Icons.Outlined.FavoriteBorder, it, Color.Red)
                }
            }
        }
    }
}

@Composable
private fun MessageAuthor(author: String) {
    Text(
        text = "@" + author,
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.labelMedium.copy(fontStyle = FontStyle.Italic)
    )
}

@Composable
private fun MessageBody(
    body: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    Text(
        text = body,
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
        modifier = modifier,
        maxLines = maxLines,
        overflow = overflow
    )
}

@Composable
private fun RepliesSection(
    replies: List<Reply>,
    currentUserId: String?,
    modifier: Modifier = Modifier,
    maxVisible: Int = Int.MAX_VALUE
) {
    if (replies.isEmpty()) return

    Column(modifier = modifier) {
        Text(
            text = "Replies (${replies.size}):",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )

        val visibleReplies = replies.take(maxVisible)
        visibleReplies.forEach { reply ->
            ReplyCard(
                reply = reply,
                isUserReply = currentUserId != null && reply.authorId == currentUserId,
                maxLines = if (maxVisible == Int.MAX_VALUE) null else 3,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }

        if (replies.size > maxVisible && maxVisible != Int.MAX_VALUE) {
            Text(
                text = "and ${replies.size - maxVisible} more replies...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
        }

    }
}

@Composable
private fun ReplyCard(
    reply: Reply,
    isUserReply: Boolean,
    maxLines: Int?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = if (isUserReply) "You" else "@${reply.authorName}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontStyle = FontStyle.Italic,
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Text(
                text = reply.body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = maxLines ?: Int.MAX_VALUE,
                overflow = if (maxLines == null) TextOverflow.Clip else TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun Reaction(
    icon: ImageVector,
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

private fun Mode.accentColor(): Color {
    return when (this) {
        Mode.SHINE -> Color.Yellow
        Mode.SHADOW -> Color.Magenta
    }
}

// Extension functions
private fun Message.toInboxMessage(): InboxMessage {
    return InboxMessage(
        id = id,
        author = authorName,
        body = body,
        mode = mode,
        up = ratingStats.likes.takeIf { it > 0 },
        down = ratingStats.dislikes.takeIf { it > 0 },
        hearts = ratingStats.superLikes.takeIf { it > 0 },
        replies = replies
    )
}

fun List<Message>.toInboxMessages(): List<InboxMessage> {
    return map { it.toInboxMessage() }
}

// Data class
data class InboxMessage(
    val id: String,
    val author: String,
    val body: String,
    val mode: String,
    val up: Int?,
    val down: Int?,
    val hearts: Int?,
    val replies: List<Reply>? = null
)

private val previewReplies = listOf(
    Reply(
        id = "reply-1",
        body = "Appreciate the vibes! Keep them coming.",
        mode = Mode.SHINE.apiValue,
        createdAt = "2024-01-01T12:00:00Z",
        authorId = "user-reply-1",
        authorName = "ReplyUser1",
        parentMessageId = "message-1"
    ),
    Reply(
        id = "reply-2",
        body = "Following along and loving it.",
        mode = Mode.SHADOW.apiValue,
        createdAt = "2024-01-02T10:15:00Z",
        authorId = "user-reply-2",
        authorName = "ReplyUser2",
        parentMessageId = "message-1"
    )
)

private val previewInboxMessage = InboxMessage(
    id = "message-1",
    author = "user_12345678",
    body = "Keep shining! This space is all about honest thoughts and bright vibes.",
    mode = Mode.SHINE.apiValue,
    up = 24,
    down = 2,
    hearts = 6,
    replies = previewReplies
)

@Preview(showBackground = true)
@Composable
private fun MessageCardPreview() {
    MadafakerTheme(mode = Mode.SHINE) {
        MessageCard(
            message = previewInboxMessage
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MessageCardReplyingPreview() {
    MadafakerTheme(mode = Mode.SHINE) {
        MessageCard(
            message = previewInboxMessage,
            isReplying = true,
            userReplies = previewReplies,
            onReplyingClosed = {},
            onSendReply = { _, _, _ -> },
            onRateMessage = { _, _ -> },
            currentUserId = previewReplies.first().authorId
        )
    }
}
