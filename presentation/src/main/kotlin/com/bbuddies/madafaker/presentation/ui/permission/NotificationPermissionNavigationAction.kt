package com.bbuddies.madafaker.presentation.ui.permission

import androidx.navigation.NavHostController
import com.bbuddies.madafaker.presentation.navigation.actions.NavigationAction
import com.bbuddies.madafaker.presentation.navigation.actions.navigateBack
import com.bbuddies.madafaker.presentation.navigation.actions.navigateToMainAndClearStack

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

}