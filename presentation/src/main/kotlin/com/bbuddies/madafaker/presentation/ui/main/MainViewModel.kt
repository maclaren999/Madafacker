package com.bbuddies.madafaker.presentation.ui.main

import androidx.lifecycle.viewModelScope
import com.bbuddies.madafaker.common_domain.AppConfig
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import com.bbuddies.madafaker.common_domain.repository.MessageRepository
import com.bbuddies.madafaker.common_domain.repository.UserRepository
import com.bbuddies.madafaker.presentation.base.BaseViewModel
import com.bbuddies.madafaker.presentation.base.UiState
import com.bbuddies.madafaker.presentation.base.suspendUiStateOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val preferenceManager: PreferenceManager
) : BaseViewModel() {

    private val _draftMessage = MutableStateFlow("")
    val draftMessage: StateFlow<String> = _draftMessage

    private val _incomingMessages = MutableStateFlow<UiState<List<Message>>>(UiState.Loading)
    val incomingMessages: StateFlow<UiState<List<Message>>> = _incomingMessages

    private val _outcomingMessages = MutableStateFlow<UiState<List<Message>>>(UiState.Loading)
    val outcomingMessages: StateFlow<UiState<List<Message>>> = _outcomingMessages

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending

    private val _currentMode = preferenceManager.currentMode
    val currentMode = _currentMode

    init {
        loadMessages()
    }

    fun onSendMessage(message: String) {
        if (message.isBlank() || _isSending.value) return

        viewModelScope.launch {
            _isSending.value = true

            val result = suspendUiStateOf {
                messageRepository.createMessage(message.trim())
            }

            when (result) {
                is UiState.Success -> {
                    _draftMessage.value = ""
                    loadOutcomingMessages()
                    showSuccess("Message sent successfully!")
                }

                is UiState.Error -> {
                    showError(result.message ?: "Failed to send message")
                }

                is UiState.Loading -> {} // Won't happen with suspendUiStateOf
            }

            _isSending.value = false
        }
    }

    fun onDraftMessageChanged(message: String) {
        if (message.length <= AppConfig.MAX_MESSAGE_LENGTH) {
            _draftMessage.value = message
        }
    }

    fun toggleMode() {
        viewModelScope.launch {
            val newMode = when (currentMode.value) {
                Mode.SHINE -> Mode.SHADOW
                Mode.SHADOW -> Mode.SHINE
            }
            preferenceManager.updateMode(newMode)
        }
    }

    fun refreshMessages() {
        loadMessages()
    }

    private fun loadMessages() {
        loadIncomingMessages()
        loadOutcomingMessages()
    }

    private fun loadIncomingMessages() {
        viewModelScope.launch {
            _incomingMessages.value = UiState.Loading
            _incomingMessages.value = suspendUiStateOf {
                messageRepository.getIncomingMassage()
            }
        }
    }

    private fun loadOutcomingMessages() {
        viewModelScope.launch {
            _outcomingMessages.value = UiState.Loading
            _outcomingMessages.value = suspendUiStateOf {
                messageRepository.getOutcomingMassage()
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