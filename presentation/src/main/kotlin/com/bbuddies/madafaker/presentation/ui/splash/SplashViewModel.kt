package com.bbuddies.madafaker.presentation.ui.splash

import androidx.compose.animation.core.MutableTransitionState
import androidx.lifecycle.viewModelScope
import com.bbuddies.madafaker.common_domain.model.User
import com.bbuddies.madafaker.common_domain.repository.UserRepository
import com.bbuddies.madafaker.presentation.NavigationItem
import com.bbuddies.madafaker.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.time.Duration

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userRepository: UserRepository
) : BaseViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _navigationEvent = MutableStateFlow<NavigationItem?>(null)
    val navigationEvent: StateFlow<NavigationItem?> = _navigationEvent

    val animationState = MutableTransitionState(false)

    init {
        fetchCurrentUser()
    }

    val userRepoAwaitTime = 5000L
    private fun fetchCurrentUser() {
        viewModelScope.launch {
            val user = runBlocking{  userRepository.getCurrentUser()  } //TODO: add 500ms timeout
            if (user != null) {
                _currentUser.value = user
                _navigationEvent.value = NavigationItem.Main
            } else {
                _currentUser.value = null
                _navigationEvent.value = NavigationItem.Account
            }
        }
    }
}