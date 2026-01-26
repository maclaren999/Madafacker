package com.bbuddies.madafaker.presentation.ui.navigation

import androidx.navigation.NavHostController
import com.bbuddies.madafaker.presentation.AccountTabRoute
import com.bbuddies.madafaker.presentation.InboxTabRoute
import com.bbuddies.madafaker.presentation.MyPostsTabRoute
import com.bbuddies.madafaker.presentation.WriteTabRoute
import com.bbuddies.madafaker.presentation.ui.main.MainTab

/**
 * Top-level destinations for the app navigation
 * These represent the main tabs accessible from the top navigation bar
 */
sealed class TopLevelDestination(
    val tab: MainTab,
    val route: Any
) {
    data object Write : TopLevelDestination(MainTab.WRITE, WriteTabRoute)
    data object MyPosts : TopLevelDestination(MainTab.MY_POSTS, MyPostsTabRoute)
    data object Inbox : TopLevelDestination(MainTab.INBOX, InboxTabRoute)
    data object Account : TopLevelDestination(MainTab.ACCOUNT, AccountTabRoute)
}

/**
 * List of all top-level destinations
 */
val topLevelDestinations = listOf(
    TopLevelDestination.Write,
    TopLevelDestination.MyPosts,
    TopLevelDestination.Inbox,
    TopLevelDestination.Account
)

/**
 * Extension function to navigate to a top-level destination
 * Handles proper back stack management for tab navigation
 */
fun NavHostController.navigateToTopLevelDestination(destination: TopLevelDestination) {
    // Prevent redundant navigation that would recreate the screen and its ViewModel
    val targetRouteName = destination.route::class.simpleName
    val currentRoute = currentBackStackEntry?.destination?.route
    if (targetRouteName != null && currentRoute?.contains(
            targetRouteName,
            ignoreCase = false
        ) == true
    ) {
        return
    }

    navigate(destination.route) {
        // Avoid multiple copies of the same destination when reselecting the same item
        launchSingleTop = true
        // Restore state when reselecting a previously selected item
        restoreState = true
    }
}

/**
 * Helper function to get TopLevelDestination from MainTab
 */
fun MainTab.toTopLevelDestination(): TopLevelDestination = when (this) {
    MainTab.WRITE -> TopLevelDestination.Write
    MainTab.MY_POSTS -> TopLevelDestination.MyPosts
    MainTab.INBOX -> TopLevelDestination.Inbox
    MainTab.ACCOUNT -> TopLevelDestination.Account
}

/**
 * Sealed class representing routes where navigation should be hidden
 * This provides type-safe route checking instead of string matching
 */
sealed class NavigationVisibility {
    /**
     * Routes where top navigation should be hidden
     */
    sealed class HiddenRoutes {
        data object Splash : HiddenRoutes()
        data object Auth : HiddenRoutes()
        data object AuthWithRedirect : HiddenRoutes()
        data object NotificationPermission : HiddenRoutes()
    }

    companion object {
        /**
         * List of route class names where navigation should be hidden
         */
        private val hiddenRouteNames = setOf(
            "SplashRoute",
            "AuthRoute",
            "AuthWithRedirectRoute",
            "NotificationPermissionRoute"
        )

        /**
         * Check if the given route should show navigation
         * @param route The current route string from NavBackStackEntry
         * @return true if navigation should be shown, false otherwise
         */
        fun shouldShowNavigation(route: String?): Boolean {
            if (route == null) return true

            // Check if route contains any of the hidden route names
            return hiddenRouteNames.none { hiddenRoute ->
                route.contains(hiddenRoute, ignoreCase = false)
            }
        }

        /**
         * Check if the given route is a top-level destination
         * @param route The current route string from NavBackStackEntry
         * @return true if route is a top-level destination
         */
        fun isTopLevelDestination(route: String?): Boolean {
            if (route == null) return false

            return topLevelDestinations.any { destination ->
                route.contains(destination.route::class.simpleName ?: "", ignoreCase = false)
            }
        }
    }
}
