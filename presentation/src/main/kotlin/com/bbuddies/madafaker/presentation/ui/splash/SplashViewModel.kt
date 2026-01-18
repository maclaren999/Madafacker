package com.bbuddies.madafaker.presentation.ui.splash

import androidx.compose.animation.core.MutableTransitionState
import androidx.lifecycle.viewModelScope
import com.bbuddies.madafaker.presentation.base.BaseViewModel
import com.bbuddies.madafaker.presentation.usecase.GetNextScreenAfterLoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private const val TAG = "SPLASH_VM"

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getNextScreenAfterLoginUseCase: GetNextScreenAfterLoginUseCase
) : BaseViewModel() {

    private val _navigationEvent = MutableStateFlow<SplashNavigationDestination?>(null)
    val navigationEvent: StateFlow<SplashNavigationDestination?> = _navigationEvent

    val animationState = MutableTransitionState(false)

    init {
        Timber.tag(TAG).d("SplashViewModel init")
        determineNextScreen()
    }

    private fun determineNextScreen() {
        viewModelScope.launch {
            try {
                Timber.tag(TAG).d("Determining next screen...")
                val nextScreen = getNextScreenAfterLoginUseCase()
                Timber.tag(TAG).d("Next screen determined: $nextScreen")
                _navigationEvent.value = nextScreen
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error determining next screen, defaulting to Auth")
                // On error, default to account creation
                _navigationEvent.value = SplashNavigationDestination.Auth
            }
        }
    }
}