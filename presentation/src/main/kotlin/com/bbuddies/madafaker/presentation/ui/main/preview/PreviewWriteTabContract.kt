package com.bbuddies.madafaker.presentation.ui.main.preview

import android.content.Context
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.presentation.base.UiState
import com.bbuddies.madafaker.presentation.ui.main.SendMessageStatus
import com.bbuddies.madafaker.presentation.ui.main.tabs.write.WriteTabContract
import com.bbuddies.madafaker.presentation.ui.main.tabs.write.WriteTabState
import com.bbuddies.madafaker.presentation.utils.SharedTextManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreviewWriteTabContract(
    draftText: String = "",
    outgoingMessages: List<Message> = emptyList(),
    currentMode: Mode = Mode.SHINE,
    sendStatus: SendMessageStatus = SendMessageStatus.Idle
) : WriteTabContract {

    private val _state = MutableStateFlow(
        WriteTabState(
            draftMessage = draftText,
            sendStatus = sendStatus,
            currentMode = currentMode,
            outcomingMessages = UiState.Success(outgoingMessages)
        )
    )

    override val state: StateFlow<WriteTabState> = _state
    override val warningsFlow = MutableStateFlow<((Context) -> String?)?>(null)
    override val sharedTextManager = SharedTextManager()

    override fun onDraftMessageChanged(message: String) {
        _state.value = _state.value.copy(draftMessage = message)
    }

    override fun onSendMessage(message: String) {}

    override fun clearDraft() {
        _state.value = _state.value.copy(draftMessage = "")
    }

    override fun refreshMessages() {}
}
