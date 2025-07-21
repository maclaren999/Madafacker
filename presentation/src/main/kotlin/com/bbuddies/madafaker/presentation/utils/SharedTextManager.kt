package com.bbuddies.madafaker.presentation.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages shared text from external apps (ACTION_SEND intents).
 * Provides a centralized way to handle text shared from other applications.
 */
@Singleton
class SharedTextManager @Inject constructor() {

    private val _sharedText = MutableStateFlow<String?>(null)
    val sharedText: StateFlow<String?> = _sharedText.asStateFlow()

    private val _hasUnconsumedSharedText = MutableStateFlow(false)
    val hasUnconsumedSharedText: StateFlow<Boolean> = _hasUnconsumedSharedText.asStateFlow()

    /**
     * Sets the shared text from an external app.
     * @param text The text to be shared, null or empty text will be ignored
     */
    fun setSharedText(text: String?) {
        val trimmedText = text?.trim()
        if (!trimmedText.isNullOrBlank()) {
            _sharedText.value = trimmedText
            _hasUnconsumedSharedText.value = true
            Timber.d("Shared text received: ${trimmedText.take(50)}...")
        } else {
            Timber.w("Received empty or null shared text, ignoring")
        }
    }

    /**
     * Consumes the shared text and marks it as consumed.
     * @return The shared text if available, null otherwise
     */
    fun consumeSharedText(): String? {
        val text = _sharedText.value
        if (text != null) {
            _hasUnconsumedSharedText.value = false
            Timber.d("Shared text consumed: ${text.take(50)}...")
        }
        return text
    }

    /**
     * Clears the shared text without consuming it.
     * Useful for cleanup or when the text is no longer needed.
     */
    fun clearSharedText() {
        _sharedText.value = null
        _hasUnconsumedSharedText.value = false
        Timber.d("Shared text cleared")
    }

    /**
     * Checks if there's unconsumed shared text available.
     * @return true if there's shared text that hasn't been consumed yet
     */
    fun hasSharedText(): Boolean {
        return _hasUnconsumedSharedText.value && !_sharedText.value.isNullOrBlank()
    }

    /**
     * Peeks at the shared text without consuming it.
     * @return The shared text if available, null otherwise
     */
    fun peekSharedText(): String? {
        return _sharedText.value
    }
}
