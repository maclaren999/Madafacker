package com.bbuddies.madafaker.presentation.navigation.actions

import androidx.navigation.NavHostController
import com.bbuddies.madafaker.presentation.ui.splash.SplashNavigationDestination

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
            is SplashNavigationDestination.Main -> navigateToMain()
            is SplashNavigationDestination.Auth -> navigateToAuth()
            is SplashNavigationDestination.NotificationPermission -> navigateToNotificationPermission()
        }
    }
    
    /**
     * Navigate to main screen from splash
     */
    fun navigateToMainFromSplash() {
        navigateToMainAndClearStack()
    }
    
    /**
     * Navigate to auth screen from splash
     */
    fun navigateToAuthFromSplash() {
        navigateToAuthAndClearStack()
    }
    
    /**
     * Navigate to notification permission screen from splash
     */
    fun navigateToNotificationPermissionFromSplash() {
        navigateToNotificationPermission()
    }
}
