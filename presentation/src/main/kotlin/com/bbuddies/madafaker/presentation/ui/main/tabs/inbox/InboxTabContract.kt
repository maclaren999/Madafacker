package com.bbuddies.madafaker.presentation.ui.main.tabs.inbox

import android.content.Context
import com.bbuddies.madafaker.common_domain.enums.MessageRating
import kotlinx.coroutines.flow.StateFlow

interface InboxTabContract {
    val state: StateFlow<InboxTabState>
    val warningsFlow: StateFlow<((context: Context) -> String?)?>

    fun refreshMessages()
    fun onSendReply(messageId: String, replyText: String, isPublic: Boolean = true)
    fun clearReplyError()
    fun clearInboxSnackbar()
    fun onRateMessage(messageId: String, rating: MessageRating)
    fun onInboxViewed()
    fun markMessageAsRead(messageId: String)
    fun onMessageTapped(messageId: String)
    fun onMessageReplyingClosed()

    /** Set highlighted message ID from deep link navigation */
    fun setHighlightedMessage(messageId: String?)
}
