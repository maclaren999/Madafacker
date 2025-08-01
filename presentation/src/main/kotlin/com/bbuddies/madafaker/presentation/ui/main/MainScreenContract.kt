package com.bbuddies.madafaker.presentation.ui.main

import android.content.Context
import com.bbuddies.madafaker.common_domain.enums.MessageRating
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.Reply
import com.bbuddies.madafaker.presentation.base.UiState
import com.bbuddies.madafaker.presentation.utils.SharedTextManager
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

    // Reply composition
    val isReplySending: StateFlow<Boolean>
    val replyError: StateFlow<String?>

    // Message highlighting
    val highlightedMessageId: StateFlow<String?>

    // Message replying state
    val replyingMessageId: StateFlow<String?>
    val userRepliesForMessage: StateFlow<List<Reply>>

    // Warnings (from BaseViewModel)
    val warningsFlow: StateFlow<((context: Context) -> String?)?>

    // Shared text manager for external text sharing
    val sharedTextManager: SharedTextManager

    // Actions
    fun onSendMessage(message: String)
    fun onDraftMessageChanged(message: String)
    fun toggleMode()
    fun refreshMessages()
    fun refreshUserData()
    fun clearDraft() // New: manually clear draft

    // Reply actions
    fun onSendReply(messageId: String, replyText: String, isPublic: Boolean = true)
    fun clearReplyError()

    // Rating actions
    fun onRateMessage(messageId: String, rating: MessageRating)

    // Message highlighting and read state
    fun onInboxViewed()
    fun markMessageAsRead(messageId: String)

    // Message interaction
    fun onMessageTapped(messageId: String)
    fun onMessageReplyingClosed()
}