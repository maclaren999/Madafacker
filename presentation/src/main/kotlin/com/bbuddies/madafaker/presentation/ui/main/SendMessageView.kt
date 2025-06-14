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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.presentation.R
import com.bbuddies.madafaker.presentation.base.UiState

/* ----------  SEND MESSAGE VIEW  ---------- */
@Composable
fun SendMessageView(viewModel: MainScreenContract) {
    val draftMessage by viewModel.draftMessage.collectAsState()
    val currentMode by viewModel.currentMode.collectAsState()
    val isSending by viewModel.isSending.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        /* ---------- MODE TOGGLE CARD ---------- */
        ModeToggleCard(
            currentMode = currentMode,
            onModeToggle = viewModel::toggleMode
        )

        /* ---------- COMPOSE CARD ---------- */
        ComposeMessageCard(
            draftMessage = draftMessage,
            isSending = isSending,
            currentMode = currentMode,
            onMessageChange = viewModel::onDraftMessageChanged,
            onSend = { viewModel.onSendMessage(draftMessage) }
        )

        /* ---------- RECENT MESSAGES CARD ---------- */
        RecentMessagesCard(viewModel)
    }
}

@Composable
private fun ModeToggleCard(
    currentMode: Mode,
    onModeToggle: () -> Unit
) {
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
                .background(MainScreenTheme.Stripe)
        )

        Row(
            modifier = Modifier
                .background(
                    color = MainScreenTheme.CardBg,
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(16.dp)
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = when (currentMode) {
                        Mode.SHINE -> stringResource(R.string.mode_shine)
                        Mode.SHADOW -> stringResource(R.string.mode_shadow)
                    },
                    color = MainScreenTheme.TextPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = when (currentMode) {
                        Mode.SHINE -> stringResource(R.string.mode_shine_description)
                        Mode.SHADOW -> stringResource(R.string.mode_shadow_description)
                    },
                    color = MainScreenTheme.TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Sun/Moon toggle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onModeToggle() }
                    .background(
                        color = when (currentMode) {
                            Mode.SHINE -> MainScreenTheme.SunBody
                            Mode.SHADOW -> Color(0xFF424242)
                        },
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (currentMode) {
                        Mode.SHINE -> "☀️"
                        Mode.SHADOW -> "🌙"
                    },
                    fontSize = 24.sp
                )
            }
        }
    }
}

@Composable
private fun ComposeMessageCard(
    draftMessage: String,
    isSending: Boolean,
    currentMode: Mode,
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
                .background(MainScreenTheme.Stripe)
        )

        Column(
            modifier = Modifier
                .background(
                    color = MainScreenTheme.CardBg,
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(20.dp)
                .weight(1f)
        ) {
            Text(
                text = when (currentMode) {
                    Mode.SHINE -> stringResource(R.string.express_positivity)
                    Mode.SHADOW -> stringResource(R.string.express_freely)
                },
                color = MainScreenTheme.TextSecondary,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Custom text field that matches the sunny theme
            SunnyTextField(
                value = draftMessage,
                onValueChange = onMessageChange,
                onSend = onSend,
                currentMode = currentMode,
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
                    text = stringResource(R.string.character_count, draftMessage.length),
                    color = if (draftMessage.length > 280) {
                        Color(0xFFE53935)
                    } else {
                        MainScreenTheme.TextSecondary
                    },
                    style = MaterialTheme.typography.labelSmall
                )

                SendButton(
                    enabled = draftMessage.isNotBlank() && draftMessage.length <= 280 && !isSending,
                    isLoading = isSending,
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
    currentMode: Mode,
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
            color = MainScreenTheme.TextPrimary,
            fontWeight = FontWeight.Medium
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Send
        ),
        keyboardActions = KeyboardActions(
            onSend = {
                if (value.isNotBlank() && value.length <= 280) {
                    onSend()
                }
            }
        ),
        decorationBox = { innerTextField ->
            Box(modifier = Modifier.fillMaxWidth()) {
                if (value.isEmpty()) {
                    Text(
                        text = when (currentMode) {
                            Mode.SHINE -> stringResource(R.string.placeholder_positive)
                            Mode.SHADOW -> stringResource(R.string.placeholder_shadow)
                        },
                        color = MainScreenTheme.TextSecondary,
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
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clickable(enabled = enabled && !isLoading) { onClick() }
            .background(
                color = when {
                    isLoading -> MainScreenTheme.TextSecondary
                    enabled -> MainScreenTheme.SunBody
                    else -> MainScreenTheme.TextSecondary
                },
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = Color.White
            )
        } else {
            Text(
                text = stringResource(R.string.button_send),
                color = Color.White,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun RecentMessagesCard(viewModel: MainScreenContract) {
    val outcomingMessages by viewModel.outcomingMessages.collectAsState()

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
                .height(160.dp)
                .background(MainScreenTheme.Stripe)
        )

        Column(
            modifier = Modifier
                .background(
                    color = MainScreenTheme.CardBg,
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(16.dp)
                .weight(1f)
        ) {
            Text(
                text = stringResource(R.string.recent_messages_title),
                color = MainScreenTheme.TextSecondary,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            when (val state = outcomingMessages) {
                UiState.Loading -> RecentMessagesLoading()

                is UiState.Success -> {
                    val recentMessages = state.data.take(3)
                    if (recentMessages.isEmpty()) {
                        RecentMessagesEmpty()
                    } else {
                        RecentMessagesList(recentMessages)
                    }
                }

                is UiState.Error -> RecentMessagesError(
                    message = state.message
                        ?: state.exception.localizedMessage
                        ?: stringResource(R.string.error_generic)
                )
            }
        }
    }
}

@Composable
private fun RecentMessagesLoading() {
    repeat(3) { index ->
        RecentMessageSkeleton()
        if (index < 2) Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun RecentMessagesEmpty() {
    Text(
        text = stringResource(R.string.no_messages_sent),
        color = MainScreenTheme.TextSecondary,
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun RecentMessagesList(messages: List<Message>) {
    messages.forEachIndexed { index, message ->
        RecentMessageItem(
            message = message.body,
            status = stringResource(R.string.message_delivered)
        )
        if (index < messages.size - 1) {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun RecentMessagesError(message: String) {
    Text(
        text = message,
        color = Color(0xFFE53935),
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun RecentMessageSkeleton() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(16.dp)
                .background(
                    color = MainScreenTheme.TextSecondary.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(4.dp)
                )
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .width(60.dp)
                .height(14.dp)
                .background(
                    color = MainScreenTheme.TextSecondary.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(4.dp)
                )
        )
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
            color = MainScreenTheme.TextPrimary,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = status,
            color = when (status) {
                stringResource(R.string.message_delivered) -> Color(0xFF4CAF50)
                stringResource(R.string.message_sending) -> MainScreenTheme.SunBody
                stringResource(R.string.message_failed) -> Color(0xFFE53935)
                else -> MainScreenTheme.TextSecondary
            },
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium
            )
        )
    }
}