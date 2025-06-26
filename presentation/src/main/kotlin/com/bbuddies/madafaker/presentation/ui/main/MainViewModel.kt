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
import com.bbuddies.madafaker.common_domain.utils.NetworkConnectivityMonitor
import com.bbuddies.madafaker.presentation.base.BaseViewModel
import com.bbuddies.madafaker.presentation.base.UiState
import com.bbuddies.madafaker.presentation.base.suspendUiStateOf
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val draftRepository: DraftRepository
) : BaseViewModel(), MainScreenContract {

    private val _draftMessage = MutableStateFlow("")
    override val draftMessage: StateFlow<String> = _draftMessage

    private val _incomingMessages = MutableStateFlow<UiState<List<Message>>>(UiState.Loading)
    override val incomingMessages: StateFlow<UiState<List<Message>>> = _incomingMessages

    private val _outcomingMessages = MutableStateFlow<UiState<List<Message>>>(UiState.Loading)
    override val outcomingMessages: StateFlow<UiState<List<Message>>> = _outcomingMessages

    private val _isSending = MutableStateFlow(false)
    override val isSending: StateFlow<Boolean> = _isSending

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
}