package com.bbuddies.madafaker.presentation.navigation.actions

import androidx.navigation.NavHostController
import com.bbuddies.madafaker.common_domain.enums.Mode

/**
 * Navigation actions for Main screen
 * Encapsulates all navigation logic from main screen
 */
class MainNavigationAction(
    override val navController: NavHostController
) : NavigationAction {
    
    /**
     * Navigate to auth screen from main (usually for logout)
     * Clears the main screen from stack
     */
    fun navigateToAuthFromMain() {
        navController.navigate(com.bbuddies.madafaker.presentation.AuthRoute) {
            popUpTo(com.bbuddies.madafaker.presentation.MainRoute) { inclusive = true }
        }
    }
    
    /**
     * Navigate to auth screen with redirect back to main
     */
    fun navigateToAuthWithRedirectToMain() {
        navigateToAuth(redirectRoute = "main")
    }
    
    /**
     * Navigate to notification permission from main
     */
    fun navigateToNotificationPermissionFromMain() {
        navigateToNotificationPermission()
    }
    
    /**
     * Handle deep link navigation within main screen
     * This could be used for navigating to specific tabs or content
     */
    fun handleDeepLinkNavigation(
        messageId: String,
        notificationId: String,
        mode: Mode
    ) {
        // For now, we stay on the same screen but could navigate to specific tab
        // This method can be extended to handle internal navigation within MainScreen
        // For example, switching to Inbox tab and highlighting specific message
    }
    
    /**
     * Navigate back from main screen (usually exits the app)
     */
    fun navigateBackFromMain() {
        // In most cases, back from main screen should exit the app
        // But we can implement custom logic here if needed
        navigateBack()
    }
}
