package com.bbuddies.madafaker.presentation.ui.main

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class TabNavigationViewModel @Inject constructor() : ViewModel(), TabNavigationContract {

    private val _currentTab = MutableStateFlow(MainTab.WRITE)
    override val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    override fun selectTab(tab: MainTab) {
        _currentTab.value = tab
    }
}
