package com.bbuddies.madafaker.presentation.ui.splash

import androidx.compose.animation.core.MutableTransitionState
import androidx.lifecycle.viewModelScope
import com.bbuddies.madafaker.common_domain.model.User
import com.bbuddies.madafaker.common_domain.repository.UserRepository
import com.bbuddies.madafaker.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : BaseViewModel() {
    private val _currentUser: MutableStateFlow<User?> = MutableStateFlow(null)
    val currentUser: StateFlow<User?> = _currentUser
    private val _isUserExsist: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isUserLocked: StateFlow<Boolean> = _isUserExsist
    val animationState = MutableTransitionState(false)

    init {
        onUserLocked()
    }

    private fun onUserLocked() {
        viewModelScope.launch {
            runCatching {
                userRepository.getCurrentUser()
            }.onFailure { exception ->
                _warningsFlow.emit { context ->
                    exception.localizedMessage
                }
            }.onSuccess { user ->
                _currentUser.value = user
                _isUserExsist.value = user != null
            }
        }
    }
}