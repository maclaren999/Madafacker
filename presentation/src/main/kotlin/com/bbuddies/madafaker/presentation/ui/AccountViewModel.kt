package com.bbuddies.madafaker.presentation.ui

import androidx.lifecycle.viewModelScope
import com.bbuddies.madafaker.common_domain.model.User
import com.bbuddies.madafaker.common_domain.repository.MessageRepository
import com.bbuddies.madafaker.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val messageRepository: MessageRepository
) : BaseViewModel() {

    private val _user = MutableStateFlow<User?>(null)

    init {
        viewModelScope.launch {
            _user.value = messageRepository.getCurrentUser()
        }
    }

    val user: StateFlow<User?> = _user.asStateFlow()

    private val _nickname = MutableStateFlow(user.value?.name ?: "Nickname")
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