package com.bbuddies.madafaker.presentation.ui.auth

import android.content.Context
import com.bbuddies.madafaker.common_domain.repository.UserRepository
import com.bbuddies.madafaker.presentation.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException

/**
 * Represents the state of nickname validation.
 */
sealed class ValidationState {
    object Success : ValidationState()
    object Loading : ValidationState()
    data class Error(val getErrorString: (context: Context) -> String) : ValidationState()
}

/**
 * Validates nickname drafts for user accounts.
 *
 * @property userRepository Repository for user-related operations.
 * @property coroutineScope Scope for managing coroutines.
 */
class NicknameDraftValidator(
    private val userRepository: UserRepository,
    private val coroutineScope: CoroutineScope
) {

    /** Current validation result. */
    val validationResult: MutableStateFlow<ValidationState?> =
        MutableStateFlow(null)

    private var validationJob: Job? = null

    /**
     * Validates a new nickname draft.
     *
     * @param newNickname The nickname to validate.
     */
    fun onDraftNickChanged(
        newNickname: String
    ) {
        validationJob?.cancel()
        validationJob = coroutineScope.launch(Dispatchers.Default) {
            validationResult.value = null
            val formatResult = hasCorrectFormat(newNickname)

            if (formatResult is ValidationState.Error) {
                validationResult.value = formatResult
                return@launch
            }

            delay(500) // Wait for user to stop typing
            launch {
                checkNameAvailability(newNickname)?.let { // Call the server
                    validationResult.value = it
                    validationJob?.cancel()
                }
            }
            delay(500)
            validationResult.value = ValidationState.Loading // Show loading if it takes too long
        }
    }

    /**
     * Checks if the nickname is available.
     *
     * @param nickname The nickname to check.
     * @return Result of the availability check.
     */
    private suspend fun checkNameAvailability(nickname: String): ValidationState? =
        runCatching {
            val isAvailable = userRepository.isNameAvailable(nickname)
            if (isAvailable) {
                ValidationState.Success
            } else {
                ValidationState.Error { context: Context ->
                    context.getString(R.string.account_nickname_is_not_available)
                }
            }
        }.getOrElse { it ->
            if (it is CancellationException)
                null
            else {
                Timber.e(it)
                ValidationState.Error { context: Context ->
                    context.getString(R.string.network_error)
                }
            }
        }

    companion object {
        /** Maximum allowed length for a nickname. */
        val MAX_NICKNAME_LENGTH = 24
    }

    /**
     * Checks if the nickname has the correct format.
     *
     * @param nickname The nickname to check.
     * @return Result of the format check.
     */
    fun hasCorrectFormat(nickname: String): ValidationState =
        when {
            nickname.isEmpty() -> ValidationState.Error { context: Context ->
                context.getString(R.string.account_nickname_cannot_be_empty)
            }

            nickname.length > MAX_NICKNAME_LENGTH -> ValidationState.Error { context: Context ->
                context.getString(R.string.account_nickname_is_too_long, MAX_NICKNAME_LENGTH)
            }

            else -> ValidationState.Success
        }
}