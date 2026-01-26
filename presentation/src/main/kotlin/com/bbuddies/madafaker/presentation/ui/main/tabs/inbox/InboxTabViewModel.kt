package com.bbuddies.madafaker.presentation.ui.main.tabs.inbox

import androidx.lifecycle.viewModelScope
import com.bbuddies.madafaker.common_domain.enums.MessageRating
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import com.bbuddies.madafaker.common_domain.repository.MessageRepository
import com.bbuddies.madafaker.common_domain.repository.UserRepository
import com.bbuddies.madafaker.common_domain.usecase.CreateReplyUseCase
import com.bbuddies.madafaker.common_domain.usecase.RateMessageUseCase
import com.bbuddies.madafaker.notification_domain.repository.NotificationManagerRepository
import com.bbuddies.madafaker.notification_domain.usecase.TrackNotificationEventUseCase
import com.bbuddies.madafaker.presentation.base.BaseViewModel
import com.bbuddies.madafaker.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InboxTabViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val createReplyUseCase: CreateReplyUseCase,
    private val rateMessageUseCase: RateMessageUseCase,
    private val trackNotificationEventUseCase: TrackNotificationEventUseCase,
    private val notificationManagerRepository: NotificationManagerRepository,
    private val preferenceManager: PreferenceManager,
    private val userRepository: UserRepository
) : BaseViewModel(), InboxTabContract {

    private val _state = MutableStateFlow(
        InboxTabState(
            currentMode = preferenceManager.currentMode.value,
            currentUserId = userRepository.currentUser.value?.id
        )
    )
    override val state: StateFlow<InboxTabState> = _state

    private var lastRefreshedUserId: String? = null

    init {
        observeIncomingMessages()
        observeAuthRefresh()
    }

    override fun refreshMessages() {
        viewModelScope.launch {
            _state.update { it.copy(incomingMessages = UiState.Loading) }
            try {
                messageRepository.refreshMessages()
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        snackbarMessage = e.message ?: "Failed to refresh messages"
                    )
                }
            }
        }
    }

    override fun onSendReply(messageId: String, replyText: String, isPublic: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isReplySending = true, replyError = null) }

            try {
                val result = createReplyUseCase(
                    body = replyText,
                    parentId = messageId,
                    isPublic = isPublic
                )

                result.fold(
                    onSuccess = {
                        _state.update { current ->
                            current.copy(
                                snackbarMessage = "Reply sent successfully!",
                                replyingMessageId = null,
                                userRepliesForMessage = emptyList()
                            )
                        }
                        trackReplyAnalytics(messageId, replyText)
                        refreshMessages()
                    },
                    onFailure = { error ->
                        _state.update {
                            it.copy(
                                replyError = error.message ?: "Failed to send reply"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _state.update { it.copy(replyError = e.message ?: "Failed to send reply") }
            } finally {
                _state.update { it.copy(isReplySending = false) }
            }
        }
    }

    override fun clearReplyError() {
        _state.update { it.copy(replyError = null) }
    }

    override fun clearInboxSnackbar() {
        _state.update { it.copy(snackbarMessage = null) }
    }

    override fun onRateMessage(messageId: String, rating: MessageRating) {
        viewModelScope.launch {
            try {
                val result = rateMessageUseCase(messageId, rating)
                result.fold(
                    onSuccess = {
                        _state.update { it.copy(snackbarMessage = "Message rated successfully!") }
                    },
                    onFailure = { error ->
                        _state.update { it.copy(snackbarMessage = "Failed to rate message: ${error.message}") }
                    }
                )
            } catch (e: Exception) {
                _state.update { it.copy(snackbarMessage = "Failed to rate message: ${e.message}") }
            }
        }
    }

    override fun onInboxViewed() {
        viewModelScope.launch {
            try {
                notificationManagerRepository.dismissAllNotifications()

                val mostRecentUnread = messageRepository.getMostRecentUnreadMessage()
                _state.update { it.copy(highlightedMessageId = mostRecentUnread?.id) }

                delay(2000)
                messageRepository.markAllIncomingMessagesAsRead()
                _state.update { it.copy(highlightedMessageId = null) }
            } catch (_: Exception) {
                // Ignore silently to avoid disrupting UX
            }
        }
    }

    override fun markMessageAsRead(messageId: String) {
        viewModelScope.launch {
            try {
                messageRepository.markMessageAsRead(messageId)
                _state.update { current ->
                    if (current.highlightedMessageId == messageId) {
                        current.copy(highlightedMessageId = null)
                    } else current
                }
            } catch (_: Exception) {
                // Ignore silently
            }
        }
    }

    override fun onMessageTapped(messageId: String) {
        viewModelScope.launch {
            try {
                val userReplies = messageRepository.getUserRepliesForMessage(messageId)
                _state.update {
                    it.copy(
                        replyingMessageId = messageId,
                        userRepliesForMessage = userReplies,
                        highlightedMessageId = it.highlightedMessageId?.takeIf { id -> id != messageId }
                    )
                }
            } catch (_: Exception) {
                // Ignore silently
            }
        }
    }

    override fun onMessageReplyingClosed() {
        _state.update {
            it.copy(
                replyingMessageId = null,
                userRepliesForMessage = emptyList()
            )
        }
    }

    override fun setHighlightedMessage(messageId: String?) {
        _state.update { it.copy(highlightedMessageId = messageId) }
    }

    private fun observeIncomingMessages() {
        combine(
            messageRepository.observeIncomingMessages(),
            preferenceManager.currentMode
        ) { messages, mode ->
            filterMessagesByMode(messages, mode) to mode
        }
            .onEach { (filtered, mode) ->
                _state.update {
                    it.copy(
                        incomingMessages = UiState.Success(filtered),
                        currentMode = mode
                    )
                }
            }
            .catch { exception ->
                _state.update { it.copy(incomingMessages = UiState.Error(exception = exception)) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeAuthRefresh() {
        userRepository.currentUser
            .onEach { user ->
                _state.update { it.copy(currentUserId = user?.id) }

                if (user == null) {
                    lastRefreshedUserId = null
                    return@onEach
                }

                if (lastRefreshedUserId != user.id) {
                    lastRefreshedUserId = user.id
                    refreshMessages()
                }
            }
            .launchIn(viewModelScope)
    }

    private fun filterMessagesByMode(messages: List<Message>, mode: Mode): List<Message> {
        return messages.filter { Mode.fromApiValue(it.mode) == mode }
    }

    private fun trackReplyAnalytics(messageId: String, replyText: String) {
        viewModelScope.launch {
            try {
                trackNotificationEventUseCase.trackMessageReplied(
                    messageId = messageId,
                    mode = preferenceManager.currentMode.value,
                    replyLength = replyText.length,
                    viaNotification = false
                )
            } catch (_: Exception) {
                // Analytics failures should not affect UX
            }
        }
    }
}
