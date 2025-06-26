package com.bbuddies.madafaker.presentation.ui.main

import android.content.Context
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.presentation.base.UiState
import kotlinx.coroutines.flow.StateFlow

interface MainScreenContract {
    // Message composition
    val draftMessage: StateFlow<String>
    val isSending: StateFlow<Boolean>

    // Messages
    val incomingMessages: StateFlow<UiState<List<Message>>>
    val outcomingMessages: StateFlow<UiState<List<Message>>>

    // Mode
    val currentMode: StateFlow<Mode>

    // Warnings (from BaseViewModel)
    val warningsFlow: StateFlow<((context: Context) -> String?)?>

    // Actions
    fun onSendMessage(message: String)
    fun onDraftMessageChanged(message: String)
    fun toggleMode()
    fun refreshMessages()
    fun clearDraft() // New: manually clear draft
}