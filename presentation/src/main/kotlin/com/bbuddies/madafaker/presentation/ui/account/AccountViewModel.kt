package com.bbuddies.madafaker.presentation.ui.account

import androidx.lifecycle.viewModelScope
import com.bbuddies.madafaker.common_domain.repository.UserRepository
import com.bbuddies.madafaker.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class AccountViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : BaseViewModel() {

    private val draftValidator = NicknameDraftValidator(userRepository, viewModelScope)
    val nicknameDraftHint = draftValidator.validationResult

    fun onDraftNickChanged(newNickname: String) {
        draftValidator.onDraftNickChanged(newNickname)
    }

    private fun handleDeleteAccount() {
        // Implement account deletion logic
        // This should ask for confirmation from user before proceeding
    }

}

