package com.bbuddies.madafaker.presentation.ui.main.preview

import android.content.Context
import com.bbuddies.madafaker.common_domain.enums.MessageRating
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.Reply
import com.bbuddies.madafaker.presentation.base.UiState
import com.bbuddies.madafaker.presentation.ui.main.tabs.inbox.InboxTabContract
import com.bbuddies.madafaker.presentation.ui.main.tabs.inbox.InboxTabState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreviewInboxTabContract(
    incomingMessages: List<Message> = emptyList(),
    currentMode: Mode = Mode.SHINE,
    highlightedMessageId: String? = null,
    replyingMessageId: String? = null,
    userReplies: List<Reply> = emptyList(),
    currentUserId: String? = null
) : InboxTabContract {

    private val _state = MutableStateFlow(
        InboxTabState(
            incomingMessages = UiState.Success(incomingMessages),
            currentMode = currentMode,
            highlightedMessageId = highlightedMessageId,
            replyingMessageId = replyingMessageId,
            userRepliesForMessage = userReplies,
            currentUserId = currentUserId
        )
    )
    override val state: StateFlow<InboxTabState> = _state
    override val warningsFlow = MutableStateFlow<((Context) -> String?)?>(null)

    override fun refreshMessages() {}
    override fun onSendReply(messageId: String, replyText: String, isPublic: Boolean) {}
    override fun clearReplyError() {}
    override fun clearInboxSnackbar() {
        _state.value = _state.value.copy(snackbarMessage = null)
    }

    override fun onRateMessage(messageId: String, rating: MessageRating) {}
    override fun onInboxViewed() {}
    override fun markMessageAsRead(messageId: String) {}
    override fun onMessageTapped(messageId: String) {
        _state.value = _state.value.copy(replyingMessageId = messageId)
    }

    override fun onMessageReplyingClosed() {
        _state.value =
            _state.value.copy(replyingMessageId = null, userRepliesForMessage = emptyList())
    }

    override fun setHighlightedMessage(messageId: String?) {
        _state.value = _state.value.copy(highlightedMessageId = messageId)
    }
}
