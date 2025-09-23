package com.bbuddies.madafaker.presentation.navigation.actions

import androidx.navigation.NavHostController
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.presentation.AuthRoute
import com.bbuddies.madafaker.presentation.AuthWithRedirectRoute
import com.bbuddies.madafaker.presentation.InboxTabWithDeepLinkRoute
import com.bbuddies.madafaker.presentation.NotificationPermissionRoute
import com.bbuddies.madafaker.presentation.SplashRoute
import com.bbuddies.madafaker.presentation.WriteTabRoute

/**
 * Base interface for navigation actions
 * Encapsulates navigation logic for each screen
 */
interface NavigationAction {
    val navController: NavHostController
}

/**
 * Extension functions for common navigation patterns
 */
fun NavigationAction.navigateToSplash() {
    navController.navigate(SplashRoute)
}

fun NavigationAction.navigateToMain() {
    navController.navigate(WriteTabRoute) {
    }
}

fun NavigationAction.navigateToMainWithDeepLink(
    messageId: String,
    notificationId: String,
    mode: Mode
) {
    navController.navigate(InboxTabWithDeepLinkRoute(messageId, notificationId, mode))
}

fun NavigationAction.navigateToAuth(redirectRoute: String? = null) {
    if (redirectRoute != null) {
        navController.navigate(AuthWithRedirectRoute(redirectRoute))
    } else {
        navController.navigate(AuthRoute)
    }
}

fun NavigationAction.navigateToNotificationPermission() {
    navController.navigate(NotificationPermissionRoute)
}

fun NavigationAction.navigateToMainAndClearStack() {
    navController.navigate(WriteTabRoute) {
        popUpTo(0) { inclusive = true }
    }
}

fun NavigationAction.navigateToAuthAndClearStack() {
    navController.navigate(AuthRoute) {
        popUpTo(0) { inclusive = true }
    }
}

fun NavigationAction.navigateBack() {
    navController.popBackStack()
}

fun NavigationAction.navigateBackTo(route: Any, inclusive: Boolean = false) {
    navController.popBackStack(route, inclusive)
}
