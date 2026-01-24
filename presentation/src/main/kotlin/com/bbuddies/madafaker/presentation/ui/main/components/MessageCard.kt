package com.bbuddies.madafaker.presentation.ui.main.components

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bbuddies.madafaker.common_domain.enums.MessageRating
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.Reply
import com.bbuddies.madafaker.presentation.R
import com.bbuddies.madafaker.presentation.design.components.MadafakerSecondaryButton
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

    val replies = userReplies.takeIf { it.isNotEmpty() } ?: message.replies.orEmpty()

    if (isReplying) {
        ReplyingMessageCard(
            message = message,
            replies = replies,
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
            modifier = modifier,
            onMessageTapped = onMessageTapped,
            onRateMessage = onRateMessage,
            currentUserId = currentUserId
        )
    }
}

@Composable
private fun ReplyingMessageCard(
    message: InboxMessage,
    replies: List<Reply>,
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
            .clickable { onReplyingClosed?.invoke() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
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
                MadafakerSecondaryButton(
                    text = if (isReplySending) "" else "Send Reply",
                    onClick = {
                        onSendReply?.invoke(message.id, replyText.trim(), true)
                        onReplyTextChange("")
                    },
                    enabled = canSend,
                    modifier = Modifier.weight(1f),
                    leadingIcon = if (isReplySending) {
                        {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onBackground,
                                strokeWidth = 2.dp
                            )
                        }
                    } else null
                )

                MadafakerSecondaryButton(
                    text = "",
                    onClick = { onReplyingClosed?.invoke() },
                    modifier = Modifier.weight(0.3f),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Reply",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                )
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
    modifier: Modifier = Modifier,
    onMessageTapped: (() -> Unit)?,
    onRateMessage: ((messageId: String, rating: MessageRating) -> Unit)?,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Absolute.SpaceBetween
            ) {
                ReplySummaryRow(
                    count = replies.size,
                )
                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ReactionWithDrawable(
                        drawableRes = R.drawable.ic_dislike,
                        count = message.dislikes ?: 0,
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        onRate = {
                            onRateMessage?.invoke(message.id, MessageRating.DISLIKE)
                        }
                    )
                    ReactionWithDrawable(
                        drawableRes = R.drawable.ic_like,
                        count = message.likes ?: 0,
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        onRate = {
                            onRateMessage?.invoke(message.id, MessageRating.LIKE)
                        }
                    )
                    ReactionWithDrawable(
                        drawableRes = R.drawable.ic_superlike,
                        count = message.superLikes ?: 0,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        onRate = {
                            onRateMessage?.invoke(message.id, MessageRating.SUPERLIKE)
                        }
                    )
                }
            }

            RepliesSection(
                replies = replies,
                currentUserId = currentUserId,
                maxVisible = 2,
                modifier = Modifier.padding(top = 8.dp)
            )
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
            color = MaterialTheme.colorScheme.onBackground,
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


@Composable
private fun ReactionWithDrawable(
    drawableRes: Int,
    count: Int,
    tint: Color,
    onRate: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(enabled = onRate != null) { onRate?.invoke() }
            .padding(4.dp)
    ) {
        Icon(
            painter = painterResource(drawableRes),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = count.toString(),
            color = tint,
            fontSize = MaterialTheme.typography.labelSmall.fontSize
        )
    }
}


// Extension functions
private fun Message.toInboxMessage(): InboxMessage {

    return InboxMessage(
        id = id,
        author = authorName,
        body = body,
        mode = mode,
        likes = ratingStats?.likes?.takeIf { it > 0 },
        dislikes = ratingStats?.dislikes?.takeIf { it > 0 },
        superLikes = ratingStats?.superLikes?.takeIf { it > 0 },
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
    val likes: Int?,
    val dislikes: Int?,
    val superLikes: Int?,
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
    likes = 24,
    dislikes = 2,
    superLikes = 6,
    replies = previewReplies
)

@Preview(showBackground = true)
@Composable
private fun MessageCardPreview() {
    MadafakerTheme(mode = Mode.SHINE) {
        MessageCard(
            message = previewInboxMessage,
            onRateMessage = { _, _ -> }
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

