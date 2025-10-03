package com.bbuddies.madafaker.presentation.ui.auth

import androidx.navigation.NavHostController
import com.bbuddies.madafaker.presentation.NotificationPermissionRoute
import com.bbuddies.madafaker.presentation.navigation.actions.NavigationAction
import com.bbuddies.madafaker.presentation.navigation.actions.navigateToMainAndClearStack
import com.bbuddies.madafaker.presentation.ui.main.MainTab
import com.bbuddies.madafaker.presentation.ui.navigation.TopLevelDestination
import com.bbuddies.madafaker.presentation.ui.navigation.navigateToTopLevelDestination
import com.bbuddies.madafaker.presentation.ui.navigation.toTopLevelDestination
import com.bbuddies.madafaker.presentation.utils.NotificationPermissionHelper

/**
 * Navigation actions for Auth screen
 * Encapsulates all navigation logic from auth screen
 * Uses TopLevelDestination pattern for cleaner navigation
 */
class AuthNavigationAction(
    override val navController: NavHostController
) : NavigationAction {

    /**
     * Navigate after successful authentication
     * Determines next screen based on notification permission status and redirect route
     *
     * @param notificationPermissionHelper Helper to check notification permission status
     * @param redirectRoute Optional redirect route (e.g., "main", "write", "inbox", etc.)
     */
    fun navigateAfterSuccessfulAuth(
        notificationPermissionHelper: NotificationPermissionHelper,
        redirectRoute: String? = null
    ) {
        when {
            // If there's a redirect route, navigate there
            redirectRoute != null -> navigateToRedirectRoute(redirectRoute)

            // Check notification permission status
            notificationPermissionHelper.isNotificationPermissionGranted() -> {
                navigateToMainAndClearStack()
            }

            // Need to request notification permission
            else -> navigateToNotificationPermissionAndClearStack()
        }
    }

    /**
     * Navigate to redirect route using TopLevelDestination pattern
     * Supports both tab routes and special routes
     */
    private fun navigateToRedirectRoute(redirectRoute: String) {
        when (redirectRoute) {
            "main" -> navigateToMainAndClearStack()
            "write" -> navigateToTopLevelTab(MainTab.WRITE)
            "my_posts" -> navigateToTopLevelTab(MainTab.MY_POSTS)
            "inbox" -> navigateToTopLevelTab(MainTab.INBOX)
            "account" -> navigateToTopLevelTab(MainTab.ACCOUNT)
            "notification_permission" -> navigateToNotificationPermissionAndClearStack()
            else -> navigateToMainAndClearStack() // Default fallback
        }
    }

    /**
     * Navigate to a top-level tab using TopLevelDestination pattern
     * Clears the auth stack
     */
    private fun navigateToTopLevelTab(tab: MainTab) {
        val destination = tab.toTopLevelDestination()
        navController.navigate(destination.route) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    /**
     * Navigate to notification permission and clear stack
     */
    private fun navigateToNotificationPermissionAndClearStack() {
        navController.navigate(NotificationPermissionRoute) {
            popUpTo(0) { inclusive = true }
        }
    }
}