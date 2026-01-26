package com.bbuddies.madafaker.presentation.ui.main

import androidx.lifecycle.viewModelScope
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import com.bbuddies.madafaker.presentation.base.BaseViewModel
import com.bbuddies.madafaker.presentation.utils.SharedTextManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager,
    private val sharedTextManagerImpl: SharedTextManager
) : BaseViewModel() {

    private val _currentTab = MutableStateFlow(MainTab.WRITE)
    val currentTab: StateFlow<MainTab> = _currentTab

    val currentMode = preferenceManager.currentMode
    val hasSeenModeToggleTip = preferenceManager.hasSeenModeToggleTip
    val sharedTextManager: SharedTextManager = sharedTextManagerImpl

    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun toggleMode() {
        viewModelScope.launch {
            val newMode = when (currentMode.value) {
                Mode.SHINE -> Mode.SHADOW
                Mode.SHADOW -> Mode.SHINE
            }
            preferenceManager.updateMode(newMode)

            if (!hasSeenModeToggleTip.value) {
                preferenceManager.setHasSeenModeToggleTip(true)
            }
        }
    }

    fun onModeToggleTipDismissed() {
        viewModelScope.launch {
            if (!hasSeenModeToggleTip.value) {
                preferenceManager.setHasSeenModeToggleTip(true)
            }
        }
    }
}
