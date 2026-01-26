package com.bbuddies.madafaker.presentation.ui.main.tabs.inbox

import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.Reply
import com.bbuddies.madafaker.presentation.base.UiState

data class InboxTabState(
    val incomingMessages: UiState<List<Message>> = UiState.Loading,
    val currentMode: Mode = Mode.SHINE,
    val highlightedMessageId: String? = null,
    val replyingMessageId: String? = null,
    val userRepliesForMessage: List<Reply> = emptyList(),
    val isReplySending: Boolean = false,
    val replyError: String? = null,
    val snackbarMessage: String? = null,
    val currentUserId: String? = null
)
