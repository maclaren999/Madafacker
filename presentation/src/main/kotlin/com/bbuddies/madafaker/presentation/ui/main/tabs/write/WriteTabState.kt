package com.bbuddies.madafaker.presentation.ui.main.tabs.write

import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.presentation.base.UiState
import com.bbuddies.madafaker.presentation.ui.main.SendMessageStatus

data class WriteTabState(
    val draftMessage: String = "",
    val sendStatus: SendMessageStatus = SendMessageStatus.Idle,
    val currentMode: Mode = Mode.SHINE,
    val hasSharedText: Boolean = false,
    val outcomingMessages: UiState<List<Message>> = UiState.Loading
) {
    /** Derived property - true when message is being sent */
    val isSending: Boolean get() = sendStatus == SendMessageStatus.Sending
}
