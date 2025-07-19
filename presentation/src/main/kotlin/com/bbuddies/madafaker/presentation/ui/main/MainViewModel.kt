package com.bbuddies.madafaker.presentation.ui.main

import androidx.lifecycle.viewModelScope
import com.bbuddies.madafaker.common_domain.AppConfig
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.AuthenticationState
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.UnsentDraft
import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import com.bbuddies.madafaker.common_domain.repository.DraftRepository
import com.bbuddies.madafaker.common_domain.repository.MessageRepository
import com.bbuddies.madafaker.common_domain.repository.UserRepository
import com.bbuddies.madafaker.common_domain.usecase.CreateReplyUseCase
import com.bbuddies.madafaker.common_domain.utils.NetworkConnectivityMonitor
import com.bbuddies.madafaker.notification_domain.repository.NotificationManagerRepository
import com.bbuddies.madafaker.notification_domain.usecase.TrackNotificationEventUseCase
import com.bbuddies.madafaker.presentation.base.BaseViewModel
import com.bbuddies.madafaker.presentation.base.UiState
import com.bbuddies.madafaker.presentation.base.suspendUiStateOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val preferenceManager: PreferenceManager,
    private val networkMonitor: NetworkConnectivityMonitor,
    private val draftRepository: DraftRepository,
    private val createReplyUseCase: CreateReplyUseCase,
    private val trackNotificationEventUseCase: TrackNotificationEventUseCase,
    private val notificationManagerRepository: NotificationManagerRepository
) : BaseViewModel(), MainScreenContract {

    private val _draftMessage = MutableStateFlow("")
    override val draftMessage: StateFlow<String> = _draftMessage

    private val _incomingMessages = MutableStateFlow<UiState<List<Message>>>(UiState.Loading)
    override val incomingMessages: StateFlow<UiState<List<Message>>> = _incomingMessages

    private val _outcomingMessages = MutableStateFlow<UiState<List<Message>>>(UiState.Loading)
    override val outcomingMessages: StateFlow<UiState<List<Message>>> = _outcomingMessages

    private val _isSending = MutableStateFlow(false)
    override val isSending: StateFlow<Boolean> = _isSending

    private val _isReplySending = MutableStateFlow(false)
    override val isReplySending: StateFlow<Boolean> = _isReplySending

    private val _replyError = MutableStateFlow<String?>(null)
    override val replyError: StateFlow<String?> = _replyError

    private val _highlightedMessageId = MutableStateFlow<String?>(null)
    override val highlightedMessageId: StateFlow<String?> = _highlightedMessageId

    override val currentMode = preferenceManager.currentMode

    val authState = userRepository.authenticationState

    val currentUser = userRepository.currentUser
    val isLoggedIn = userRepository.isUserLoggedIn

    fun handleAuthState() {
        viewModelScope.launch {
            authState.collect { state ->
                when (state) {
                    is AuthenticationState.NotAuthenticated -> {
                        //TODO
                        // Navigate to login
                    }

                    is AuthenticationState.Loading -> {
                        // Show loading
                    }

                    is AuthenticationState.Authenticated -> {
                        // Use state.user (guaranteed non-null!)
                        val user = state.user
                        // ... use user
                    }

                    is AuthenticationState.Error -> {
                        // Handle error
                    }
                }
            }
        }
    }

    init {
        refreshMessages()
        observeMessages()
        restoreDraft()
        setupDraftAutoSave()
    }

    private fun restoreDraft() {
        viewModelScope.launch {
            try {
                val savedDraft = draftRepository.getDraftOnce()
                if (savedDraft != null && savedDraft.body.isNotBlank()) {
                    _draftMessage.value = savedDraft.body
                }
            } catch (e: Exception) {
                // Silently fail - draft restoration is not critical
            }
        }
    }

    private fun setupDraftAutoSave() {
        viewModelScope.launch {
            // Auto-save drafts with debouncing to avoid excessive saves
            _draftMessage
                .drop(1) // Skip initial empty value
                .distinctUntilChanged()
                .debounce(1000) // Wait 1 second after user stops typing
                .collect { message ->
                    if (message.isNotBlank()) {
                        saveDraft(message)
                    }
                }
        }
    }

    private suspend fun saveDraft(message: String) {
        try {
            val draft = UnsentDraft(
                body = message,
                mode = currentMode.value.apiValue,
                timestamp = System.currentTimeMillis()
            )
            draftRepository.saveDraft(draft)
        } catch (e: Exception) {
            // Silently fail - draft saving is not critical
        }
    }

    override fun onSendMessage(message: String) {
        if (message.isBlank() || _isSending.value) return

        viewModelScope.launch {
            _isSending.value = true

            val result = suspendUiStateOf {
                messageRepository.createMessage(message.trim())
            }

            when (result) {
                is UiState.Success -> {
                    _draftMessage.value = ""
                    clearDraft() // Clear draft after successful sending
                }

                is UiState.Error -> {
                    showError(result.message ?: "Failed to send message")
                }

                is UiState.Loading -> {} // Won't happen with suspendUiStateOf
            }

            _isSending.value = false
        }
    }

    override fun onDraftMessageChanged(message: String) {
        if (message.length <= AppConfig.MAX_MESSAGE_LENGTH) {
            _draftMessage.value = message
        }
    }

    override fun toggleMode() {
        viewModelScope.launch {
            val newMode = when (currentMode.value) {
                Mode.SHINE -> Mode.SHADOW
                Mode.SHADOW -> Mode.SHINE
            }
            preferenceManager.updateMode(newMode)

            // Save draft with new mode if there's content
            if (_draftMessage.value.isNotBlank()) {
                saveDraft(_draftMessage.value)
            }
        }
    }

    override fun clearDraft() {
        viewModelScope.launch {
            try {
                draftRepository.clearDraft()
                _draftMessage.value = ""
            } catch (e: Exception) {
                // Silently fail
            }
        }
    }

    private fun observeMessages() {
        // Observe incoming messages
        messageRepository.observeIncomingMessages()
            .onEach { messages ->
                _incomingMessages.value = UiState.Success(messages)
            }
            .catch { exception ->
                _incomingMessages.value = UiState.Error(exception = exception)
            }
            .launchIn(viewModelScope)

        // Observe outgoing messages
        messageRepository.observeOutgoingMessages()
            .onEach { messages ->
                _outcomingMessages.value = UiState.Success(messages)
            }
            .catch { exception ->
                _outcomingMessages.value = UiState.Error(exception = exception)
            }
            .launchIn(viewModelScope)
    }

    override fun refreshMessages() {
        viewModelScope.launch {
            try {
                messageRepository.refreshMessages()
            } catch (e: Exception) {
                showError("Failed to refresh messages: ${e.message}")
            }
        }
    }

    private fun showSuccess(message: String) {
        viewModelScope.launch {
            _warningsFlow.emit { _ -> message }
        }
    }

    private fun showError(message: String) {
        viewModelScope.launch {
            _warningsFlow.emit { _ -> message }
        }
    }

    override fun onSendReply(messageId: String, replyText: String, isPublic: Boolean) {
        viewModelScope.launch {
            _isReplySending.value = true
            _replyError.value = null

            try {
                val result = createReplyUseCase(
                    body = replyText,
                    parentId = messageId,
                    isPublic = isPublic
                )

                result.fold(
                    onSuccess = { reply ->
                        showSuccess("Reply sent successfully!")

                        // Track reply analytics
                        viewModelScope.launch {
                            try {
                                trackNotificationEventUseCase.trackMessageReplied(
                                    messageId = messageId,
                                    mode = currentMode.value,
                                    replyLength = replyText.length,
                                    viaNotification = false // This is from the inbox, not notification
                                )
                            } catch (e: Exception) {
                                // Silently fail analytics tracking
                            }
                        }

                        // Optionally refresh messages to show the new reply
                        refreshMessages()
                    },
                    onFailure = { error ->
                        _replyError.value = error.message ?: "Failed to send reply"
                    }
                )
            } catch (e: Exception) {
                _replyError.value = e.message ?: "Failed to send reply"
            } finally {
                _isReplySending.value = false
            }
        }
    }

    override fun clearReplyError() {
        _replyError.value = null
    }

    override fun onInboxViewed() {
        viewModelScope.launch {
            try {
                // Auto-dismiss all notifications
                notificationManagerRepository.dismissAllNotifications()

                // Get most recent unread message for highlighting
                val mostRecentUnread = messageRepository.getMostRecentUnreadMessage()
                _highlightedMessageId.value = mostRecentUnread?.id

                // Track organic inbox viewing - use simple analytics for now
                // TODO: Replace with proper trackCustomEvent when available

                // Mark all messages as read after a delay (user has seen them)
                delay(2000) // 2 second delay to ensure user has seen the content
                messageRepository.markAllIncomingMessagesAsRead()

                // TODO: Refactor highlight behaviour to reflect where user focus is
                // Clear highlighting after messages are marked as read
                _highlightedMessageId.value = null

            } catch (e: Exception) {
                // Silently handle errors - don't disrupt user experience
            }
        }
    }

    override fun markMessageAsRead(messageId: String) {
        viewModelScope.launch {
            try {
                messageRepository.markMessageAsRead(messageId)

                // Track message viewed - simplified for now
                // TODO: Add proper message viewed tracking

                // If this was the highlighted message, clear highlighting
                if (_highlightedMessageId.value == messageId) {
                    _highlightedMessageId.value = null
                }

            } catch (e: Exception) {
                // Silently handle errors
            }
        }
    }
}