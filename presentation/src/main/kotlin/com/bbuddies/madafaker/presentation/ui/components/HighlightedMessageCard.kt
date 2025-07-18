package com.bbuddies.madafaker.presentation.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.presentation.ui.main.tabs.InboxMessage

@Composable
fun HighlightedMessageCard(
    message: InboxMessage,
    modifier: Modifier = Modifier
) {
    var selectedRating by remember { mutableStateOf<String?>(null) }
    var replyText by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(true) } // Start expanded for highlighted messages

    val mode = Mode.fromApiValue(message.mode)
    val accentColor = when (mode) {
        Mode.SHINE -> Color(0xFFFFD700) // Gold
        Mode.SHADOW -> Color(0xFF6A5ACD) // Purple
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .border(
                width = 2.dp,
                color = accentColor,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Mode indicator with enhanced styling
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = accentColor,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${mode.displayName} ${getModeIcon(mode)}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "📍 From notification",
                    style = MaterialTheme.typography.bodySmall,
                    color = accentColor,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Message content with enhanced styling
            Text(
                text = message.body,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 24.sp
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Rating buttons - always visible for highlighted messages
            Text(
                text = "Rate this message:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
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

            // Reply section
            Text(
                text = "Reply to this message:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
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
                        color = accentColor.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                decorationBox = { innerTextField ->
                    if (replyText.isEmpty()) {
                        Text(
                            text = "Tap to reply to this message...",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Send reply button
            Button(
                onClick = {
                    // TODO: Implement reply sending
                    replyText = ""
                },
                enabled = replyText.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor
                )
            ) {
                Text(
                    text = "Send Reply",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun RatingButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent,
            contentColor = if (isSelected) color else MaterialTheme.colorScheme.onSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) color else MaterialTheme.colorScheme.outline
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 12.sp
        )
    }
}

private fun getModeIcon(mode: Mode): String {
    return when (mode) {
        Mode.SHINE -> "☀️"
        Mode.SHADOW -> "🌙"
    }
}
