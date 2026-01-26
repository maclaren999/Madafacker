package com.bbuddies.madafaker.presentation.ui.main.tabs.myposts

import androidx.lifecycle.viewModelScope
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import com.bbuddies.madafaker.common_domain.repository.MessageRepository
import com.bbuddies.madafaker.presentation.base.BaseViewModel
import com.bbuddies.madafaker.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
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
class MyPostsTabViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val preferenceManager: PreferenceManager
) : BaseViewModel(), MyPostsTabContract {

    private val _state = MutableStateFlow(
        MyPostsTabState(currentMode = preferenceManager.currentMode.value)
    )
    override val state: StateFlow<MyPostsTabState> = _state

    init {
        observeMode()
        observeOutgoingMessages()
        refreshMessages()
    }

    override fun refreshMessages() {
        viewModelScope.launch {
            _state.update { it.copy(outcomingMessages = UiState.Loading) }
            try {
                messageRepository.refreshMessages()
            } catch (e: Exception) {
                _warningsFlow.emit { _ -> e.message ?: "Failed to refresh messages" }
            }
        }
    }

    private fun observeMode() {
        preferenceManager.currentMode
            .onEach { mode -> _state.update { it.copy(currentMode = mode) } }
            .launchIn(viewModelScope)
    }

    private fun observeOutgoingMessages() {
        combine(
            messageRepository.observeOutgoingMessages(),
            preferenceManager.currentMode
        ) { messages, mode ->
            filterMessagesByMode(messages, mode)
        }
            .onEach { filtered ->
                _state.update { it.copy(outcomingMessages = UiState.Success(filtered)) }
            }
            .catch { exception ->
                _state.update { it.copy(outcomingMessages = UiState.Error(exception = exception)) }
            }
            .launchIn(viewModelScope)
    }

    private fun filterMessagesByMode(messages: List<Message>, mode: Mode): List<Message> {
        return messages.filter { Mode.fromApiValue(it.mode) == mode }
    }
}
