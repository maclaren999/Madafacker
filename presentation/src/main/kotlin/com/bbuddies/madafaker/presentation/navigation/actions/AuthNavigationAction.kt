package com.bbuddies.madafaker.presentation.navigation.actions

import androidx.navigation.NavHostController
import com.bbuddies.madafaker.presentation.utils.NotificationPermissionHelper

/**
 * Navigation actions for Auth screen
 * Encapsulates all navigation logic from auth screen
 */
class AuthNavigationAction(
    override val navController: NavHostController
) : NavigationAction {
    
    /**
     * Navigate after successful authentication
     * Determines next screen based on notification permission status
     */
    fun navigateAfterSuccessfulAuth(
        notificationPermissionHelper: NotificationPermissionHelper,
        redirectRoute: String? = null
    ) {
        when {
            // If there's a redirect route, navigate there
            redirectRoute != null -> {
                when (redirectRoute) {
                    "main" -> navigateToMainAndClearStack()
                    "notification_permission" -> navigateToNotificationPermissionAndClearStack()
                    else -> navigateToMainAndClearStack() // Default fallback
                }
            }
            
            // Check notification permission status
            notificationPermissionHelper.isNotificationPermissionGranted() -> {
                navigateToMainAndClearStack()
            }
            
            // Need to request notification permission
            else -> {
                navigateToNotificationPermissionAndClearStack()
            }
        }
    }
    
    /**
     * Navigate to main screen after auth, clearing the auth stack
     */
    fun navigateToMainAfterAuth() {
        navigateToMainAndClearStack()
    }
    
    /**
     * Navigate to notification permission screen after auth
     */
    fun navigateToNotificationPermissionAfterAuth() {
        navigateToNotificationPermissionAndClearStack()
    }
    
    /**
     * Navigate back from auth screen
     */
    fun navigateBackFromAuth() {
        navigateBack()
    }
    
    /**
     * Navigate to notification permission and clear stack
     */
    private fun navigateToNotificationPermissionAndClearStack() {
        navController.navigate(com.bbuddies.madafaker.presentation.NotificationPermissionRoute) {
            popUpTo(0) { inclusive = true }
        }
    }
}
