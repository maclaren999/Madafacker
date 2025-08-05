package com.bbuddies.madafaker.presentation.navigation.actions

import androidx.navigation.NavHostController

/**
 * Navigation actions for Notification Permission screen
 * Encapsulates all navigation logic from notification permission screen
 */
class NotificationPermissionNavigationAction(
    override val navController: NavHostController
) : NavigationAction {
    
    /**
     * Navigate to main screen after permission is granted or skipped
     * Clears the permission screen from stack
     */
    fun navigateToMainAfterPermission() {
        navigateToMainAndClearStack()
    }
    
    /**
     * Navigate back from notification permission screen
     * Usually goes back to auth screen
     */
    fun navigateBackFromPermission() {
        navigateBack()
    }
    
    /**
     * Navigate to main screen when permission is granted
     */
    fun navigateAfterPermissionGranted() {
        navigateToMainAfterPermission()
    }
    
    /**
     * Navigate to main screen when permission is denied/skipped
     */
    fun navigateAfterPermissionDenied() {
        navigateToMainAfterPermission()
    }
    
    /**
     * Navigate to main screen when user skips permission
     */
    fun navigateAfterPermissionSkipped() {
        navigateToMainAfterPermission()
    }
    
    /**
     * Handle navigation when user opens settings
     * Usually stays on the same screen and waits for user to return
     */
    fun handleSettingsNavigation() {
        // No navigation needed - user will return to this screen
        // This method can be used for analytics or state management
    }
}
