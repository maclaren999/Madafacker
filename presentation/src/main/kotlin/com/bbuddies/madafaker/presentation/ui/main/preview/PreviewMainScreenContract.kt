package com.bbuddies.madafaker.presentation.ui.main.preview

import android.content.Context
import com.bbuddies.madafaker.common_domain.enums.MessageRating
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.MessageState
import com.bbuddies.madafaker.common_domain.model.Reply
import com.bbuddies.madafaker.presentation.base.UiState
import com.bbuddies.madafaker.presentation.ui.main.MainScreenContract
import com.bbuddies.madafaker.presentation.ui.main.MainTab
import com.bbuddies.madafaker.presentation.ui.main.SendMessageStatus
import com.bbuddies.madafaker.presentation.utils.SharedTextManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Shared preview implementation of MainScreenContract for Compose previews.
 * Provides default preview data and no-op implementations of actions.
 */
class PreviewMainScreenContract(
    draftText: String = "",
    outgoingMessages: List<Message> = emptyList(),
    incomingMessages: List<Message> = emptyList(),
    currentMode: Mode = Mode.SHINE,
    currentTab: MainTab = MainTab.WRITE,
    sendStatus: SendMessageStatus = SendMessageStatus.Idle,
    highlightedMessageId: String? = null,
    replyingMessageId: String? = null,
    userReplies: List<Reply> = emptyList()
) : MainScreenContract {
    private val sharedTextManagerImpl = SharedTextManager()

    override val draftMessage = MutableStateFlow(draftText)
    override val isSending = MutableStateFlow(false)
    override val sendStatus = MutableStateFlow(sendStatus)
    override val incomingMessages: StateFlow<UiState<List<Message>>> =
        MutableStateFlow(UiState.Success(incomingMessages))
    override val outcomingMessages: StateFlow<UiState<List<Message>>> =
        MutableStateFlow(UiState.Success(outgoingMessages))
    override val currentMode = MutableStateFlow(currentMode)
    override val currentTab = MutableStateFlow(currentTab)
    override val isReplySending = MutableStateFlow(false)
    override val replyError = MutableStateFlow<String?>(null)
    override val highlightedMessageId = MutableStateFlow(highlightedMessageId)
    override val replyingMessageId = MutableStateFlow(replyingMessageId)
    override val userRepliesForMessage = MutableStateFlow(userReplies)
    override val warningsFlow = MutableStateFlow<((Context) -> String?)?>(null)
    override val inboxSnackbarMessage = MutableStateFlow<String?>(null)
    override val sharedTextManager = sharedTextManagerImpl

    override fun onSendMessage(message: String) {}
    override fun onDraftMessageChanged(message: String) {
        draftMessage.value = message
    }

    override fun toggleMode() {
        currentMode.value = when (currentMode.value) {
            Mode.SHINE -> Mode.SHADOW
            Mode.SHADOW -> Mode.SHINE
        }
    }

    override fun refreshMessages() {}
    override fun refreshUserData() {}
    override fun clearDraft() {
        draftMessage.value = ""
    }

    override fun selectTab(tab: MainTab) {
        currentTab.value = tab
    }

    override fun onSendReply(messageId: String, replyText: String, isPublic: Boolean) {}
    override fun clearReplyError() {
        replyError.value = null
    }
    override fun clearInboxSnackbar() {
        inboxSnackbarMessage.value = null
    }

    override fun onRateMessage(messageId: String, rating: MessageRating) {}
    override fun onInboxViewed() {}
    override fun markMessageAsRead(messageId: String) {}
    override fun onMessageTapped(messageId: String) {}
    override fun onMessageReplyingClosed() {}
}

/**
 * Sample messages for preview purposes.
 */
object PreviewMessages {
    val sentMessage = Message(
        id = "1",
        body = "Недавнее отправленное сообщение",
        mode = Mode.SHINE.apiValue,
        createdAt = "",
        authorId = "me",
        authorName = "Preview User",
        localState = MessageState.SENT
    )

    val failedMessage = Message(
        id = "2",
        body = "Сообщение в очереди",
        mode = Mode.SHINE.apiValue,
        createdAt = "",
        authorId = "me",
        authorName = "Preview User",
        localState = MessageState.FAILED
    )

    val sampleOutgoing = listOf(sentMessage, failedMessage)
}
