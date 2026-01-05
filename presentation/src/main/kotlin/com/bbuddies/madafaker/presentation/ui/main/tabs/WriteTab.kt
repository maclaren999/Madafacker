package com.bbuddies.madafaker.presentation.ui.main.tabs

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bbuddies.madafaker.common_domain.enums.MessageRating
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.MessageState
import com.bbuddies.madafaker.common_domain.model.Reply
import com.bbuddies.madafaker.presentation.base.UiState
import com.bbuddies.madafaker.presentation.design.theme.MadafakerTheme
import com.bbuddies.madafaker.presentation.ui.main.MainScreenContract
import com.bbuddies.madafaker.presentation.ui.main.MainTab
import com.bbuddies.madafaker.presentation.ui.main.SendMessageView
import com.bbuddies.madafaker.presentation.utils.SharedTextManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun WriteTab(viewModel: MainScreenContract) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SendMessageView(viewModel)
    }
}

private val previewOutgoingMessages = listOf(
    Message(
        id = "sent-1",
        body = "Shared a reminder to drink water today.",
        mode = Mode.SHINE.apiValue,
        isPublic = true,
        createdAt = "2024-04-01T08:00:00Z",
        updatedAt = "2024-04-01T08:05:00Z",
        authorId = "preview-user",
        replies = emptyList(),
        localState = MessageState.SENT
    ),
    Message(
        id = "sent-2",
        body = "Drafting a note about handling tough days.",
        mode = Mode.SHADOW.apiValue,
        isPublic = true,
        createdAt = "2024-04-02T10:00:00Z",
        updatedAt = "2024-04-02T10:05:00Z",
        authorId = "preview-user",
        replies = emptyList(),
        localState = MessageState.PENDING
    )
)

private class PreviewWriteContract(
    initialMessages: List<Message> = previewOutgoingMessages
) : MainScreenContract {
    override val draftMessage: StateFlow<String> = MutableStateFlow("Write something encouraging...")
    override val isSending: StateFlow<Boolean> = MutableStateFlow(false)
    override val incomingMessages: StateFlow<UiState<List<Message>>> =
        MutableStateFlow(UiState.Success(emptyList()))
    override val outcomingMessages: StateFlow<UiState<List<Message>>> =
        MutableStateFlow(UiState.Success(initialMessages))
    override val currentMode: StateFlow<Mode> = MutableStateFlow(Mode.SHINE)
    override val currentTab: StateFlow<MainTab> = MutableStateFlow(MainTab.WRITE)
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
private fun WriteTabPreview() {
    MadafakerTheme(mode = Mode.SHINE) {
        WriteTab(viewModel = PreviewWriteContract())
    }
}
