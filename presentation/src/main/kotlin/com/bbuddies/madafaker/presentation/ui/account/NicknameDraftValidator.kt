package com.bbuddies.madafaker.presentation.ui.account

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.bbuddies.madafaker.common_domain.repository.UserRepository
import com.bbuddies.madafaker.presentation.R
import com.bbuddies.madafaker.presentation.base.MfResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NicknameDraftValidator(
    private val userRepository: UserRepository,
    private val coroutineScope: CoroutineScope
) {

    private var validationJob: Job? = null
    val validationResult = mutableStateOf<MfResult<(Context) -> String>>(MfResult.Success({ _ -> "" }))

    fun onDraftNickChanged(
        newNickname: String
    ) {
        validationJob?.cancel()
        validationJob = coroutineScope.launch(Dispatchers.IO) {
            delay(500) // Wait for user to stop typing
            validationResult.value = MfResult.Loading()
            delay(500)
            val hasCorrectFormat = hasCorrectFormat(newNickname)
            if (hasCorrectFormat is MfResult.Success) {
                validationResult.value = runCatching {
                    val isAvailable = userRepository.isNameAvailable(newNickname)
                    if (isAvailable)
                        MfResult.Success { context: Context ->
                            context.getString(R.string.account_nickname_is_available)
                        }
                    else
                        MfResult.Error { context: Context ->
                            context.getString(R.string.account_nickname_is_not_available)
                        }
                }.getOrElse { _ ->
                    MfResult.Error({ context: Context ->
                        context.getString(R.string.network_error)
                    })
                }
            } else if (hasCorrectFormat is MfResult.Error) {
                validationResult.value = hasCorrectFormat
            }
        }
    }

    val MAX_NICKNAME_LENGTH = 100
    fun hasCorrectFormat(nickname: String): MfResult<(Context) -> String> =
        when {
            nickname.isEmpty() -> MfResult.Error(
                { context: Context -> context.getString(R.string.account_nickname_cannot_be_empty) }
            )

            nickname.length > MAX_NICKNAME_LENGTH -> MfResult.Error({ context: Context ->
                context.getString(
                    R.string.account_characters_max_length
                )
            })

            else -> MfResult.Success({ _ -> "" })
        }
}