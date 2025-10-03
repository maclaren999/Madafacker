package com.bbuddies.madafaker.presentation.ui.splash

import androidx.navigation.NavHostController
import com.bbuddies.madafaker.presentation.navigation.actions.NavigationAction
import com.bbuddies.madafaker.presentation.navigation.actions.navigateToAuthAndClearStack
import com.bbuddies.madafaker.presentation.navigation.actions.navigateToMainAndClearStack
import com.bbuddies.madafaker.presentation.navigation.actions.navigateToNotificationPermission

/**
 * Navigation destinations for splash screen
 */
sealed class SplashNavigationDestination {
    object Main : SplashNavigationDestination()
    object Auth : SplashNavigationDestination()
    object NotificationPermission : SplashNavigationDestination()
}
/**
 * Navigation actions for Splash screen
 * Encapsulates all navigation logic from splash screen
 */
class SplashNavigationAction(
    override val navController: NavHostController
) : NavigationAction {

    /**
     * Navigate based on splash screen determination
     */
    fun navigateBasedOnDestination(destination: SplashNavigationDestination) {
        when (destination) {
            is SplashNavigationDestination.Main -> navigateToMainAndClearStack()
            is SplashNavigationDestination.Auth ->  navigateToAuthAndClearStack()
            is SplashNavigationDestination.NotificationPermission -> navigateToNotificationPermission()
        }
    }
}
