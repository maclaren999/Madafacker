package com.bbuddies.madafaker.presentation.ui.splash

import androidx.compose.animation.core.MutableTransitionState
import androidx.lifecycle.viewModelScope
import com.bbuddies.madafaker.presentation.NavigationItem
import com.bbuddies.madafaker.presentation.base.BaseViewModel
import com.bbuddies.madafaker.presentation.usecase.GetNextScreenAfterLoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getNextScreenAfterLoginUseCase: GetNextScreenAfterLoginUseCase
) : BaseViewModel() {

    private val _navigationEvent = MutableStateFlow<NavigationItem?>(null)
    val navigationEvent: StateFlow<NavigationItem?> = _navigationEvent

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
                _navigationEvent.value = NavigationItem.Account
            }
        }
    }
}