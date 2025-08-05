package com.bbuddies.madafaker.presentation.ui.splash

import androidx.compose.animation.core.MutableTransitionState
import androidx.lifecycle.viewModelScope
import com.bbuddies.madafaker.presentation.base.BaseViewModel
import com.bbuddies.madafaker.presentation.usecase.GetNextScreenAfterLoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Navigation destinations for splash screen
 */
sealed class SplashNavigationDestination(val route: String) {
    object Main : SplashNavigationDestination("MainScreen")
    object Auth : SplashNavigationDestination("AuthScreen")
    object NotificationPermission : SplashNavigationDestination("NotificationPermissionScreen")
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getNextScreenAfterLoginUseCase: GetNextScreenAfterLoginUseCase
) : BaseViewModel() {

    private val _navigationEvent = MutableStateFlow<SplashNavigationDestination?>(null)
    val navigationEvent: StateFlow<SplashNavigationDestination?> = _navigationEvent

    val animationState = MutableTransitionState(false)

    init {
        determineNextScreen()
    }

    private fun determineNextScreen() {
        viewModelScope.launch {
            try {
                val nextScreen = getNextScreenAfterLoginUseCase()
                _navigationEvent.value = nextScreen
            } catch (e: Exception) {
                // On error, default to account creation
                _navigationEvent.value = SplashNavigationDestination.Auth
            }
        }
    }
}