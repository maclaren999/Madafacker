package com.bbuddies.madafaker.presentation.ui.account

import android.content.Context
import com.bbuddies.madafaker.common_domain.repository.UserRepository
import com.bbuddies.madafaker.presentation.R
import com.bbuddies.madafaker.presentation.base.MfResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException

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
    val validationResult: MutableStateFlow<MfResult<Unit>?> =
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
        validationJob = coroutineScope.launch(Dispatchers.IO) {
            validationResult.value = null
            val formatResult = hasCorrectFormat(newNickname)

            if (formatResult is MfResult.Error) {
                validationResult.value = formatResult
                return@launch
            }

            delay(500) // Wait for user to stop typing
            checkNameAvailability(newNickname)?.let { // Call the server
                validationResult.value = it
                return@launch
            }
            delay(500)
            validationResult.value = MfResult.Loading() // Show loading if it takes too long
        }
    }

    /**
     * Checks if the nickname is available.
     *
     * @param nickname The nickname to check.
     * @return Result of the availability check.
     */
    private suspend fun checkNameAvailability(nickname: String): MfResult<Unit>? =
        runCatching {
            val isAvailable = userRepository.isNameAvailable(nickname)
            if (isAvailable) {
                MfResult.Success(Unit)
            } else {
                MfResult.Error({ context: Context ->
                    context.getString(R.string.account_nickname_is_not_available)
                }, Unit)
            }
        }.getOrElse { it ->
            if (it is CancellationException)
                null
            else {
                Timber.e(it)
                MfResult.Error({ context: Context ->
                    context.getString(R.string.network_error)
                })
            }
        }


    /** Maximum allowed length for a nickname. */
    val MAX_NICKNAME_LENGTH = 100

    /**
     * Checks if the nickname has the correct format.
     *
     * @param nickname The nickname to check.
     * @return Result of the format check.
     */
    fun hasCorrectFormat(nickname: String): MfResult<Unit> =
        when {
            nickname.isEmpty() -> MfResult.Error({ context: Context ->
                context.getString(R.string.account_nickname_cannot_be_empty)
            })

            nickname.length > MAX_NICKNAME_LENGTH -> MfResult.Error({ context: Context ->
                context.getString(R.string.account_nickname_is_too_long, MAX_NICKNAME_LENGTH)
            })

            else -> MfResult.Success(Unit)
        }
}