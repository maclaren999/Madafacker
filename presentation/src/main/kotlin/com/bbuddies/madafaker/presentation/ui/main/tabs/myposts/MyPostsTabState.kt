package com.bbuddies.madafaker.presentation.ui.main.tabs.myposts

import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.MessageWithReplies
import com.bbuddies.madafaker.presentation.base.UiState

data class MyPostsTabState(
    val outcomingMessages: UiState<List<MessageWithReplies>> = UiState.Loading,
    val currentMode: Mode = Mode.SHINE
)
