package com.bbuddies.madafaker.presentation.ui.main.tabs.write

import android.content.Context
import com.bbuddies.madafaker.presentation.utils.SharedTextManager
import kotlinx.coroutines.flow.StateFlow

interface WriteTabContract {
    val state: StateFlow<WriteTabState>
    val warningsFlow: StateFlow<((context: Context) -> String?)?>
    val sharedTextManager: SharedTextManager

    fun onDraftMessageChanged(message: String)
    fun onSendMessage(message: String)
    fun clearDraft()
    fun refreshMessages()
}
