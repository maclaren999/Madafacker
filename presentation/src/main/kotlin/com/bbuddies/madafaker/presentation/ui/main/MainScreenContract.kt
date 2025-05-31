package com.bbuddies.madafaker.presentation.ui.main

import kotlinx.coroutines.flow.StateFlow

interface MainScreenContract {
    val draftMessage: StateFlow<String>
    fun onSendMessage(message: String)
    fun onDraftMessageChanged(message: String)
}