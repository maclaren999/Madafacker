package com.bbuddies.madafaker.presentation.ui

import com.bbuddies.madafaker.presentation.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel : BaseViewModel() {

    private val _nickname = MutableStateFlow("")
    val nickname: StateFlow<String> = _nickname.asStateFlow()

    private val _fullName = MutableStateFlow("")
    val fullName: StateFlow<String> = _fullName.asStateFlow()

    fun updateNickname(newNickname: String) {
        if (newNickname.length <= 100) {
            _nickname.value = newNickname
        }
    }

    fun logOut() {
        // Implement log out logic
        // For example, clear user session, navigate to login screen, etc.
    }

    fun deleteAccount() {
        // Implement account deletion logic
        // This should probably show a confirmation dialog before proceeding
    }

}