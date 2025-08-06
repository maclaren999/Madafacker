package com.bbuddies.madafaker.presentation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.DeepLinkData
import com.bbuddies.madafaker.presentation.ui.auth.AuthScreen
import com.bbuddies.madafaker.presentation.ui.main.MainScreen
import com.bbuddies.madafaker.presentation.ui.main.MainViewModel
import com.bbuddies.madafaker.presentation.ui.permission.NotificationPermissionScreen
import com.bbuddies.madafaker.presentation.ui.splash.SplashScreen
import com.bbuddies.madafaker.presentation.navigation.actions.AuthNavigationAction
import com.bbuddies.madafaker.presentation.navigation.actions.MainNavigationAction
import com.bbuddies.madafaker.presentation.navigation.actions.NotificationPermissionNavigationAction
import com.bbuddies.madafaker.presentation.navigation.actions.SplashNavigationAction
import kotlinx.serialization.Serializable

// ============================================================================
// TYPE-SAFE ROUTES using Kotlin Serialization
// ============================================================================

@Serializable
object SplashRoute

@Serializable
object MainRoute

@Serializable
data class MainWithDeepLinkRoute(
    val messageId: String,
    val notificationId: String,
    val mode: Mode
)

@Serializable
object AuthRoute

@Serializable
data class AuthWithRedirectRoute(
    val redirectRoute: String? = null
)

@Serializable
object NotificationPermissionRoute

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: Any = SplashRoute,
    deepLinkData: DeepLinkData? = null
) {
    // Handle deep link navigation
    LaunchedEffect(deepLinkData) {
        if (deepLinkData != null && deepLinkData.isValid()) {
            navController.navigate(MainWithDeepLinkRoute(
                messageId = deepLinkData.messageId,
                notificationId = deepLinkData.notificationId,
                mode = deepLinkData.mode
            ))
        }
    }

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash Screen
        composable<SplashRoute> {
            val splashNavAction = SplashNavigationAction(navController)

            SplashScreen(
                navAction = splashNavAction,
                splashViewModel = hiltViewModel(),
                modifier = Modifier.fillMaxSize()
            )
        }

        // Main Screen (simple)
        composable<MainRoute> {
            val mainNavAction = MainNavigationAction(navController)

            MainScreen(
                navAction = mainNavAction,
                viewModel = hiltViewModel<MainViewModel>(),
                deepLinkData = null,
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .windowInsetsPadding(WindowInsets.ime)
            )
        }

        // Main Screen with Deep Link
        composable<MainWithDeepLinkRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<MainWithDeepLinkRoute>()
            val deepLink = DeepLinkData(
                messageId = route.messageId,
                notificationId = route.notificationId,
                mode = route.mode
            )
            val mainNavAction = MainNavigationAction(navController)

            MainScreen(
                navAction = mainNavAction,
                viewModel = hiltViewModel<MainViewModel>(),
                deepLinkData = deepLink,
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .windowInsetsPadding(WindowInsets.ime)
            )
        }

        // Auth Screen (simple)
        composable<AuthRoute> {
            val authNavAction = AuthNavigationAction(navController)

            AuthScreen(
                navAction = authNavAction,
                viewModel = hiltViewModel(),
                redirectRoute = null,
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .windowInsetsPadding(WindowInsets.ime)
            )
        }

        // Auth Screen with Redirect
        composable<AuthWithRedirectRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<AuthWithRedirectRoute>()
            val authNavAction = AuthNavigationAction(navController)

            AuthScreen(
                navAction = authNavAction,
                viewModel = hiltViewModel(),
                redirectRoute = route.redirectRoute,
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .windowInsetsPadding(WindowInsets.ime)
            )
        }

        // Notification Permission Screen
        composable<NotificationPermissionRoute> {
            val permissionNavAction = NotificationPermissionNavigationAction(navController)

            NotificationPermissionScreen(
                navAction = permissionNavAction,
                viewModel = hiltViewModel(),
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .windowInsetsPadding(WindowInsets.ime)
            )
        }
    }
}

