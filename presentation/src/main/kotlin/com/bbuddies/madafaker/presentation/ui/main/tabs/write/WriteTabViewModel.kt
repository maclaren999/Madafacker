package com.bbuddies.madafaker.presentation.ui.main.tabs.write

import androidx.lifecycle.viewModelScope
import com.bbuddies.madafaker.common_domain.AppConfig
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.MessageSendException
import com.bbuddies.madafaker.common_domain.model.UnsentDraft
import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import com.bbuddies.madafaker.common_domain.repository.DraftRepository
import com.bbuddies.madafaker.common_domain.repository.MessageRepository
import com.bbuddies.madafaker.presentation.base.BaseViewModel
import com.bbuddies.madafaker.presentation.base.UiState
import com.bbuddies.madafaker.presentation.base.suspendUiStateOf
import com.bbuddies.madafaker.presentation.ui.main.SendMessageStatus
import com.bbuddies.madafaker.presentation.utils.SharedTextManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WriteTabViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val draftRepository: DraftRepository,
    private val sharedTextManagerImpl: SharedTextManager,
    private val preferenceManager: PreferenceManager
) : BaseViewModel(), WriteTabContract {

    private val _state = MutableStateFlow(
        WriteTabState(currentMode = preferenceManager.currentMode.value)
    )
    override val state: StateFlow<WriteTabState> = _state
    override val sharedTextManager: SharedTextManager = sharedTextManagerImpl

    override fun onDraftMessageChanged(message: String) {
        if (message.length <= AppConfig.MAX_MESSAGE_LENGTH) {
            _state.update {
                it.copy(
                    draftMessage = message,
                    sendStatus = when (it.sendStatus) {
                        is SendMessageStatus.Error,
                        SendMessageStatus.Success -> SendMessageStatus.Idle

                        else -> it.sendStatus
                    }
                )
            }
        }
    }

    override fun onSendMessage(message: String) {
        if (message.isBlank() || state.value.isSending) return

        viewModelScope.launch {
            _state.update { it.copy(sendStatus = SendMessageStatus.Sending) }

            val result = suspendUiStateOf {
                messageRepository.createMessage(message)
            }

            when (result) {
                is UiState.Success -> handleSendSuccess()
                is UiState.Error -> handleSendError(result.exception)
                is UiState.Loading -> Unit
            }
        }
    }

    override fun clearDraft() {
        viewModelScope.launch {
            try {
                draftRepository.clearDraft()
                _state.update { it.copy(draftMessage = "") }
            } catch (_: Exception) {
                // Draft clearing failure is not critical; ignore silently
            }
        }
    }

    override fun refreshMessages() {
        viewModelScope.launch {
            try {
                messageRepository.refreshMessages()
            } catch (e: Exception) {
                _warningsFlow.emit { _ -> e.message ?: "Failed to refresh messages" }
            }
        }
    }

    init {
        observeMode()
        observeOutgoingMessages()
        restoreDraft()
        setupDraftAutoSave()
        setupSharedTextHandling()
        refreshMessages()
    }

    private fun observeMode() {
        preferenceManager.currentMode
            .onEach { mode ->
                _state.update { current -> current.copy(currentMode = mode) }

                val currentDraft = _state.value.draftMessage
                if (currentDraft.isNotBlank()) {
                    viewModelScope.launch { saveDraft(currentDraft, mode) }
                }
            }
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

    private fun restoreDraft() {
        viewModelScope.launch {
            try {
                val savedDraft = draftRepository.getDraftOnce()
                if (savedDraft != null && savedDraft.body.isNotBlank()) {
                    _state.update { it.copy(draftMessage = savedDraft.body) }
                }
            } catch (_: Exception) {
                // Ignore silently; draft restoration is non-critical
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun setupDraftAutoSave() {
        viewModelScope.launch {
            state
                .map { it.draftMessage }
                .drop(1)
                .distinctUntilChanged()
                .debounce(1000)
                .collect { message ->
                    if (message.isNotBlank()) {
                        saveDraft(message, preferenceManager.currentMode.value)
                    }
                }
        }
    }

    private suspend fun saveDraft(message: String, mode: Mode) {
        try {
            val draft = UnsentDraft(
                body = message,
                mode = mode.apiValue,
                timestamp = System.currentTimeMillis()
            )
            draftRepository.saveDraft(draft)
        } catch (_: Exception) {
            // Silently ignore; draft saving should not block UI
        }
    }

    private fun setupSharedTextHandling() {
        // Track flag for UI (e.g., navigation triggers)
        sharedTextManagerImpl.hasUnconsumedSharedText
            .onEach { hasShared ->
                _state.update { it.copy(hasSharedText = hasShared) }
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            sharedTextManagerImpl.sharedText.collect { sharedText ->
                if (sharedText != null && sharedTextManagerImpl.hasUnconsumedSharedText.value) {
                    val currentDraft = _state.value.draftMessage
                    if (currentDraft.isBlank() || currentDraft.length < 10) {
                        val consumedText = sharedTextManagerImpl.consumeSharedText()
                        if (consumedText != null) {
                            val truncatedText =
                                if (consumedText.length > AppConfig.MAX_MESSAGE_LENGTH) {
                                    consumedText.take(AppConfig.MAX_MESSAGE_LENGTH - 3) + "..."
                                } else {
                                    consumedText
                                }
                            _state.update { it.copy(draftMessage = truncatedText) }
                        }
                    } else {
                        sharedTextManagerImpl.clearSharedText()
                    }
                }
            }
        }
    }

    private fun handleSendSuccess() {
        _state.update {
            it.copy(
                draftMessage = "",
                sendStatus = SendMessageStatus.Success
            )
        }

        clearDraft()

        viewModelScope.launch {
            delay(1500)
            _state.update { current ->
                if (current.sendStatus is SendMessageStatus.Success) {
                    current.copy(sendStatus = SendMessageStatus.Idle)
                } else {
                    current
                }
            }
        }
    }

    private fun handleSendError(error: Throwable) {
        val mapped = when (error) {
            is MessageSendException -> SendMessageStatus.Error(
                message = mapSendErrorMessage(error),
                errorCode = error.errorCode
            )

            else -> SendMessageStatus.Error()
        }

        _state.update { it.copy(sendStatus = mapped) }
    }

    private fun mapSendErrorMessage(error: MessageSendException): String? {
        return when (error.errorCode) {
            else -> error.errorMessage
        }
    }
}
