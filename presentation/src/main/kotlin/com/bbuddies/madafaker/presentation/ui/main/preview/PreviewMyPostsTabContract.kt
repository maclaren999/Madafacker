package com.bbuddies.madafaker.presentation.ui.main.preview

import android.content.Context
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.presentation.base.UiState
import com.bbuddies.madafaker.presentation.ui.main.tabs.myposts.MyPostsTabContract
import com.bbuddies.madafaker.presentation.ui.main.tabs.myposts.MyPostsTabState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreviewMyPostsTabContract(
    outgoingMessages: List<Message> = emptyList(),
    currentMode: Mode = Mode.SHINE
) : MyPostsTabContract {

    private val _state = MutableStateFlow(
        MyPostsTabState(
            outcomingMessages = UiState.Success(outgoingMessages),
            currentMode = currentMode
        )
    )
    override val state: StateFlow<MyPostsTabState> = _state
    override val warningsFlow = MutableStateFlow<((Context) -> String?)?>(null)

    override fun refreshMessages() {}
}
