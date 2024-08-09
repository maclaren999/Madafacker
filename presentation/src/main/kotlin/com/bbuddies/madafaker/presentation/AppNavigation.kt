package com.bbuddies.madafaker.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

enum class MadafakerScreen() {
    SplashScreen,
    MainScreen,
    AccountScreen,
    MessageScreen
}

sealed class NavigationItem(val route: String) {
    object Splash : NavigationItem(MadafakerScreen.SplashScreen.name)
    object Main : NavigationItem(MadafakerScreen.MainScreen.name)
    object Message : NavigationItem(MadafakerScreen.MessageScreen.name)
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
            SplashScreen(navController)
        }
        composable(NavigationItem.Main.route) {
            MainScreen(navController)
        }

    }
}