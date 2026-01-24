package com.bbuddies.madafaker.presentation.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bbuddies.madafaker.common_domain.AppConfig
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.MessageState
import com.bbuddies.madafaker.presentation.R
import com.bbuddies.madafaker.presentation.base.UiState
import com.bbuddies.madafaker.presentation.design.components.MadafakerSecondaryButton
import com.bbuddies.madafaker.presentation.design.components.MadafakerTextField
import com.bbuddies.madafaker.presentation.design.components.MessageStateIndicator
import com.bbuddies.madafaker.presentation.design.theme.MadafakerTheme
import com.bbuddies.madafaker.presentation.ui.main.preview.PreviewMainScreenContract
import com.bbuddies.madafaker.presentation.ui.main.preview.PreviewMessages
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun SendMessageView(viewModel: MainScreenContract) {
    val draftMessage by viewModel.draftMessage.collectAsState()
    val currentMode by viewModel.currentMode.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val sendStatus by viewModel.sendStatus.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        MessageCard(
            draftMessage = draftMessage,
            isSending = isSending,
            sendStatus = sendStatus,
            currentMode = currentMode,
            onMessageChange = viewModel::onDraftMessageChanged,
            onSend = { viewModel.onSendMessage(draftMessage) }
        )

        RecentMessagesCard(viewModel)
    }
}

@Composable
private fun MessageCard(
    draftMessage: String,
    isSending: Boolean,
    sendStatus: SendMessageStatus,
    currentMode: Mode,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Column {
        // Action-prompting copy
//        Text(
//            text = when (currentMode) {
//                Mode.SHINE -> stringResource(R.string.express_positivity)
//                Mode.SHADOW -> stringResource(R.string.express_freely)
//            },
//            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
//            style = MaterialTheme.typography.headlineMedium,
//        )

        SunnyTextField(
            value = draftMessage,
            onValueChange = onMessageChange,
            onSend = onSend,
            enabled = !isSending,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Send button and character count
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${draftMessage.length}/${AppConfig.MAX_MESSAGE_LENGTH}",
                color = if (draftMessage.length > AppConfig.MAX_MESSAGE_LENGTH) {
                    Color(0xFFE53935)
                } else {
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                },
                style = MaterialTheme.typography.bodyMedium
            )

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                SendStatusIndicator(status = sendStatus)
            }

            MadafakerSecondaryButton(
                enabled = draftMessage.isNotBlank() &&
                    draftMessage.length <= AppConfig.MAX_MESSAGE_LENGTH &&
                    !isSending,
                onClick = onSend,
                text = stringResource(R.string.button_send)
            )
        }
    }
}

@Composable
private fun SunnyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    MadafakerTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = false,
        modifier = modifier.padding(16.dp),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Send
        ),
        keyboardActions = KeyboardActions(
            onSend = {
                if (value.isNotBlank() && value.length <= AppConfig.MAX_MESSAGE_LENGTH) {
                    onSend()
                }
            }
        ),
    )
}

@Composable
private fun SendStatusIndicator(status: SendMessageStatus) {
    when (status) {
        SendMessageStatus.Idle -> Unit
        SendMessageStatus.Sending -> RetroDotsLoader()
        SendMessageStatus.Success -> Text(
            text = stringResource(R.string.send_status_ok),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        is SendMessageStatus.Error -> {
            val message = status.message?.takeIf { it.isNotBlank() }
                ?: stringResource(R.string.message_send_failed)
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RetroDotsLoader() {
    var dotCount by remember { mutableStateOf(1) }

    LaunchedEffect(Unit) {
        while (isActive) {
            dotCount = if (dotCount == 3) 1 else dotCount + 1
            delay(350)
        }
    }

    Text(
        text = ".".repeat(dotCount),
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
    )
}

@Composable
private fun RecentMessagesCard(viewModel: MainScreenContract) {
    val outcomingMessages by viewModel.outcomingMessages.collectAsState()
    val state = outcomingMessages
    if (state is UiState.Success && state.data.isEmpty()) {
        return
    }

    Column {
        Text(
            text = stringResource(R.string.recent_messages_title),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        when (state) {
            UiState.Loading -> RecentMessagesLoading()

            is UiState.Success -> {
                val recentMessages = state.data.take(3)
                RecentMessagesList(recentMessages)
            }

            is UiState.Error -> RecentMessagesError(
                message = state.message
                    ?: state.exception.localizedMessage
                    ?: stringResource(R.string.error_generic)
            )
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
private fun RecentMessagesList(messages: List<Message>) {
    messages.forEachIndexed { index, message ->
        RecentMessageItem(
            message = message.body,
            messageState = message.localState
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
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .width(60.dp)
                .height(14.dp)
        )
    }
}

@Composable
private fun RecentMessageItem(
    message: String,
    messageState: MessageState
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        MessageStateIndicator(messageState)

    }
}

@Preview(showBackground = true)
@Composable
private fun SendMessageViewPreview() {
    MadafakerTheme(mode = Mode.SHINE) {
        SendMessageView(
            viewModel = PreviewMainScreenContract(
                draftText = "Расскажи что-нибудь хорошее о сегодняшнем дне",
                outgoingMessages = PreviewMessages.sampleOutgoing
            )
        )
    }
}





















