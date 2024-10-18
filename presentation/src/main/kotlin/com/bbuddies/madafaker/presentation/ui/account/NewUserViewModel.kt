package com.bbuddies.madafaker.presentation.ui.account

import androidx.lifecycle.viewModelScope
import com.bbuddies.madafaker.common_domain.repository.UserRepository
import com.bbuddies.madafaker.presentation.base.BaseViewModel
import com.bbuddies.madafaker.presentation.base.MfResult
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Assumes that user is completely new.
 */
@HiltViewModel
class NewUserViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : BaseViewModel() {

    private val _draftNickname = MutableStateFlow("")
    val draftNickname: StateFlow<String> = _draftNickname

    private val draftValidator = NicknameDraftValidator(userRepository, viewModelScope)
    val nicknameDraftValidationResult = draftValidator.validationResult

    fun onDraftNickChanged(newNickname: String) {
        _draftNickname.value = newNickname
        draftValidator.onDraftNickChanged(newNickname)
    }


    fun onSaveNickname(onSuccessfulSave: () -> Unit) {
        if (nicknameDraftValidationResult.value is MfResult.Success) {


            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Timber.w(task.exception, "Fetching FCM registration token failed")
                    return@addOnCompleteListener
                }
                val token = task.result
                sendUserDataToServer(onSuccessfulSave, token)
            }
        }
    }

    private fun sendUserDataToServer(onSuccessfulSave: () -> Unit, fcmToken: String) {
        viewModelScope.launch {
            runCatching {
                userRepository.createUser(_draftNickname.value, fcmToken)
            }.onFailure { exception ->
                _warningsFlow.emit { context ->
                    exception.localizedMessage
                }
            }.onSuccess {
                onSuccessfulSave()
            }
        }
    }

    fun handleDeleteAccount() {
        //TODO: Implement
    }
}
