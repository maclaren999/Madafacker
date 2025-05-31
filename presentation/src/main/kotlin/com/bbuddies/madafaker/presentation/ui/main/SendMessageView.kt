package com.bbuddies.madafaker.presentation.ui.main


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


/* ----------  SEND MESSAGE VIEW  ---------- */
@Composable
fun SendMessageView(viewModel: MainViewModel) {
    val draftMessage by viewModel.draftMessage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        /* ---------- MODE TOGGLE CARD ---------- */
        ModeToggleCard()

        /* ---------- COMPOSE CARD ---------- */
        ComposeMessageCard(
            draftMessage = draftMessage,
            onMessageChange = { viewModel.onDraftMessageChanged(it) },
            onSend = { viewModel.onSendMessage(draftMessage) }
        )

        /* ---------- RECENT MESSAGES CARD ---------- */
        RecentMessagesCard()
    }
}

@Composable
private fun ModeToggleCard() {
    var isShineMode by remember { mutableStateOf(true) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawWithContent {
                drawContent()
                // Subtle inner shadow
                drawRect(color = Color.Black.copy(alpha = 0.04f))
            }
    ) {
        // Left gold stripe
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(80.dp)
                .background(Stripe)
        )

        Row(
            modifier = Modifier
                .background(
                    color = CardBg,
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(16.dp)
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isShineMode) "shine mode" else "shadow mode",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = if (isShineMode) "positive vibes only" else "uncensored thoughts",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Sun/Moon toggle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { isShineMode = !isShineMode }
                    .background(
                        color = if (isShineMode) SunBody else Color(0xFF424242),
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isShineMode) "☀️" else "🌙",
                    fontSize = 24.sp
                )
            }
        }
    }
}

@Composable
private fun ComposeMessageCard(
    draftMessage: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawWithContent {
                drawContent()
                drawRect(color = Color.Black.copy(alpha = 0.04f))
            }
    ) {
        // Left gold stripe
        Box(
            modifier = Modifier
                .width(4.dp)
                .defaultMinSize(minHeight = 200.dp)
                .background(Stripe)
        )

        Column(
            modifier = Modifier
                .background(
                    color = CardBg,
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(20.dp)
                .weight(1f)
        ) {
            Text(
                text = "express yourself",
                color = TextSecondary,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Custom text field that matches the sunny theme
            SunnyTextField(
                value = draftMessage,
                onValueChange = onMessageChange,
                onSend = onSend,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Send button and character count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${draftMessage.length}/280",
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelSmall
                )

                SendButton(
                    enabled = draftMessage.isNotBlank(),
                    onClick = onSend
                )
            }
        }
    }
}

@Composable
private fun SunnyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .background(
                color = Color.White.copy(alpha = 0.8f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
            .defaultMinSize(minHeight = 120.dp),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = TextPrimary,
            fontWeight = FontWeight.Medium
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Send
        ),
        keyboardActions = KeyboardActions(
            onSend = { onSend() }
        ),
        decorationBox = { innerTextField ->
            Box(modifier = Modifier.fillMaxWidth()) {
                if (value.isEmpty()) {
                    Text(
                        text = "what's on your mind?",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun SendButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clickable(enabled = enabled) { onClick() }
            .background(
                color = if (enabled) SunBody else TextSecondary,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text = "send",
            color = Color.White,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun RecentMessagesCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawWithContent {
                drawContent()
                drawRect(color = Color.Black.copy(alpha = 0.04f))
            }
    ) {
        // Left gold stripe
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(160.dp)
                .background(Stripe)
        )

        Column(
            modifier = Modifier
                .background(
                    color = CardBg,
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(16.dp)
                .weight(1f)
        ) {
            Text(
                text = "your recent messages",
                color = TextSecondary,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Placeholder for recent messages
            repeat(3) { index ->
                RecentMessageItem(
                    message = "Sample message ${index + 1}...",
                    status = if (index == 0) "delivered" else "pending"
                )
                if (index < 2) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun RecentMessageItem(
    message: String,
    status: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = message,
            color = TextPrimary,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = status,
            color = if (status == "delivered") Color(0xFF4CAF50) else TextSecondary,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}