package com.bbuddies.madafaker.presentation.ui.main.tabs.myposts

import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.presentation.base.UiState

data class MyPostsTabState(
    val outcomingMessages: UiState<List<Message>> = UiState.Loading,
    val currentMode: Mode = Mode.SHINE
)
