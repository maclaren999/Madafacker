package com.bbuddies.madafaker.presentation.ui.main.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ThumbUp
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bbuddies.madafaker.common_domain.AppConfig
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.Reply


@Composable
fun MessageCard(
    message: InboxMessage,
    modifier: Modifier = Modifier,
    isReplying: Boolean = false,
    userReplies: List<Reply> = emptyList(),
    onMessageTapped: (() -> Unit)? = null,
    onSendReply: ((messageId: String, replyText: String, isPublic: Boolean) -> Unit)? = null,
    onReplyingClosed: (() -> Unit)? = null,
    isReplySending: Boolean = false,
    replyError: String? = null
) {
    var selectedRating by remember { mutableStateOf<String?>(null) }
    var replyText by remember { mutableStateOf("") }

    val mode = Mode.fromApiValue(message.mode)
    val accentColor = when (mode) {
        Mode.SHINE -> Color(0xFFFFD700) // Gold
        Mode.SHADOW -> Color(0xFF6A5ACD) // Purple
    }

    if (isReplying) {
        // Replying state - enhanced interactive card
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
                // Message content
                Text(
                    text = message.author,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelMedium,
                )

                Text(
                    text = message.body,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                // Show existing user replies if any
                if (userReplies.isNotEmpty()) {
                    Text(
                        text = "Your previous replies:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    userReplies.forEach { reply ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Transparent
                            )
                        ) {
                            Text(
                                text = reply.body,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(8.dp),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Rating buttons
                Text(
                    text = "Rate this message:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    RatingButton(
                        icon = Icons.Default.Close,
                        label = "Dislike",
                        isSelected = selectedRating == "dislike",
                        color = Color.Red,
                        onClick = { selectedRating = "dislike" }
                    )

                    RatingButton(
                        icon = Icons.Default.ThumbUp,
                        label = "Like",
                        isSelected = selectedRating == "like",
                        color = Color.Green,
                        onClick = { selectedRating = "like" }
                    )

                    RatingButton(
                        icon = Icons.Default.Favorite,
                        label = "Superlike",
                        isSelected = selectedRating == "superlike",
                        color = Color.Magenta,
                        onClick = { selectedRating = "superlike" }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reply input
                Text(
                    text = "Reply to this message:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                BasicTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    decorationBox = { innerTextField ->
                        if (replyText.isEmpty()) {
                            Text(
                                text = "Tap to reply to this message...",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        innerTextField()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Send reply button
                    Button(
                        onClick = {
                            onSendReply?.invoke(message.id, replyText, true)
                            replyText = ""
                        },
                        enabled = replyText.isNotBlank() && !isReplySending,
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

                    // Close button
                    OutlinedButton(
                        onClick = { onReplyingClosed?.invoke() },
                        modifier = Modifier.weight(0.3f)
                    ) {
                        Text("Close")
                    }
                }

                // Show reply error if any
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
    } else {
        // Default state - simple card with click handler
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable { onMessageTapped?.invoke() }
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
            Text(
                text = message.author,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelMedium,
            )

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
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                message.up?.let {
                    Reaction(Icons.Outlined.ThumbUp, it, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
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
}

@Composable
private fun RatingButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent,
            contentColor = if (isSelected) color else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        ),
        modifier = Modifier.size(width = 80.dp, height = 40.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) color else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Text(
                text = label,
                fontSize = 8.sp,
                textAlign = TextAlign.Center
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

// Extension functions
@Suppress("KotlinConstantConditions")
private fun Message.toInboxMessage(): InboxMessage {
    return InboxMessage(
        id = id,
        author = "user_${authorId.take(8)}", // Simplified author display
        body = body,
        mode = mode,
        up = if (AppConfig.USE_MOCK_API) (0..20).random() else null,
        down = if (AppConfig.USE_MOCK_API) (0..5).random() else null,
        hearts = if (AppConfig.USE_MOCK_API) (0..15).random() else null
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
    val hearts: Int?
)