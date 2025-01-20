package com.bbuddies.madafaker.presentation.ui.account

import androidx.lifecycle.viewModelScope
import com.bbuddies.madafaker.common_domain.repository.UserRepository
import com.bbuddies.madafaker.presentation.base.BaseViewModel
import com.bbuddies.madafaker.presentation.base.MfResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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
            viewModelScope.launch {
                runCatching {
                    userRepository.createUser(_draftNickname.value)
                }.onFailure { exception ->
                    _warningsFlow.emit { context ->
                        exception.localizedMessage ?: "An error occurred"
                    }
                }.onSuccess {
                    onSuccessfulSave()
                }
            }
        }
    }

    fun handleDeleteAccount() {
        //TODO: Implement
    }
}