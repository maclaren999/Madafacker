package com.bbuddies.madafaker.presentation.ui.main

import kotlinx.coroutines.flow.StateFlow

/**
 * Contract for Tab Navigation functionality
 * Manages tab selection and navigation state
 */
interface TabNavigationContract {
    
    // Current selected tab
    val currentTab: StateFlow<MainTab>
    
    // Actions
    fun selectTab(tab: MainTab)
}
