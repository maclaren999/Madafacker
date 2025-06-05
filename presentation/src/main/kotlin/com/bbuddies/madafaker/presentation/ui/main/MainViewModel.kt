package com.bbuddies.madafaker.presentation.ui.main

import androidx.lifecycle.viewModelScope
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.repository.MessageRepository
import com.bbuddies.madafaker.common_domain.repository.UserRepository
import com.bbuddies.madafaker.presentation.base.BaseViewModel
import com.bbuddies.madafaker.presentation.base.MfResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository
) : BaseViewModel(), MainScreenContract {

    private val _draftMessage = MutableStateFlow("")
    override val draftMessage: StateFlow<String> = _draftMessage

    private val _incomingMessages = MutableStateFlow<MfResult<List<Message>>>(MfResult.Loading())
    val incomingMessages: StateFlow<MfResult<List<Message>>> = _incomingMessages

    private val _outcomingMessages = MutableStateFlow<MfResult<List<Message>>>(MfResult.Loading())
    val outcomingMessages: StateFlow<MfResult<List<Message>>> = _outcomingMessages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadMessages()
    }

    private fun loadMessages() {
        loadIncomingMessages()
        loadOutcomingMessages()
    }

    override fun onSendMessage(message: String) {
        if (message.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            runCatching {
                messageRepository.createMessage(message.trim())
            }.onFailure { exception ->
                _warningsFlow.emit { context ->
                    exception.localizedMessage ?: "Failed to send message"
                }
            }.onSuccess {
                _draftMessage.value = ""
                loadOutcomingMessages() // Refresh outcoming messages
            }
            _isLoading.value = false
        }
    }

    override fun onDraftMessageChanged(message: String) {
        _draftMessage.value = message
    }


    private fun loadIncomingMessages() {
        viewModelScope.launch {
            _incomingMessages.value = MfResult.Loading()
            runCatching {
                messageRepository.getIncomingMassage()
            }.onSuccess { messages ->
                _incomingMessages.value = MfResult.Success(messages)
            }.onFailure { exception ->
                _incomingMessages.value = MfResult.Error(
                    getErrorString = { context ->
                        exception.localizedMessage ?: "Failed to load incoming messages"
                    }
                )
            }
        }
    }

    private fun loadOutcomingMessages() {
        viewModelScope.launch {
            _outcomingMessages.value = MfResult.Loading()
            runCatching {
                messageRepository.getOutcomingMassage()
            }.onSuccess { messages ->
                _outcomingMessages.value = MfResult.Success(messages)
                _outcomingMessages.value = MfResult.Error(
                    getErrorString = { context ->
                        "Failed to load outcoming messages"
                    }
                )
            }
        }
    }

    fun refreshMessages() {
        loadMessages()
    }
}