package com.bbuddies.madafaker.presentation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bbuddies.madafaker.presentation.ui.account.AuthScreen
import com.bbuddies.madafaker.presentation.ui.main.MainScreen
import com.bbuddies.madafaker.presentation.ui.main.MainViewModel
import com.bbuddies.madafaker.presentation.ui.permission.NotificationPermissionScreen
import com.bbuddies.madafaker.presentation.ui.splash.SplashScreen

enum class MadafakerScreen {
    SplashScreen,
    MainScreen,
    MessageScreen,
    AuthScreen,
    NotificationPermissionScreen,
}

sealed class NavigationItem(val route: String) {
    object Splash : NavigationItem(MadafakerScreen.SplashScreen.name)
    object Main : NavigationItem(MadafakerScreen.MainScreen.name)

    //    object Message : NavigationItem(MadafakerScreen.MessageScreen.name)
    object Account : NavigationItem(MadafakerScreen.AuthScreen.name)
    object NotificationPermission : NavigationItem(MadafakerScreen.NotificationPermissionScreen.name)
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: String = NavigationItem.Splash.route,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NavigationItem.Splash.route) {
            SplashScreen(
                navController = navController,
                splashViewModel = hiltViewModel(),
                modifier = Modifier.fillMaxSize()
            )
        }
        composable(NavigationItem.Main.route) {
            MainScreen(
                navController = navController,
                viewModel = hiltViewModel<MainViewModel>(),
                modifier = Modifier
                    .fillMaxSize()
                    // Handle navigation bars and keyboard for main screen
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .windowInsetsPadding(WindowInsets.ime)
            )
        }
//        composable(NavigationItem.Message.route) {
//            MessageScreen(navController)
//        }
        composable(NavigationItem.Account.route) {
            AuthScreen(
                navController = navController,
                viewModel = hiltViewModel(),
                modifier = Modifier
                    .fillMaxSize()
                    // Handle keyboard insets for input screen
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .windowInsetsPadding(WindowInsets.ime)
            )
        }
        composable(NavigationItem.NotificationPermission.route) {
            NotificationPermissionScreen(
                navController = navController,
                viewModel = hiltViewModel(),
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .windowInsetsPadding(WindowInsets.ime)
            )
        }
    }
}