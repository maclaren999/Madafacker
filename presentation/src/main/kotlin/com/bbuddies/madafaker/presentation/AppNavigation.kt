package com.bbuddies.madafaker.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bbuddies.madafaker.presentation.ui.account.SetNicknameScreen
import com.bbuddies.madafaker.presentation.ui.main.MainScreen
import com.bbuddies.madafaker.presentation.ui.splash.SplashScreen

enum class MadafakerScreen() {
    SplashScreen,
    MainScreen,
    MessageScreen,
    SetNicknameScreen,
}

sealed class NavigationItem(val route: String) {
    object Splash : NavigationItem(MadafakerScreen.SplashScreen.name)
    object Main : NavigationItem(MadafakerScreen.MainScreen.name)

    //    object Message : NavigationItem(MadafakerScreen.MessageScreen.name)
    object Account : NavigationItem(MadafakerScreen.SetNicknameScreen.name)
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
            SplashScreen(navController, hiltViewModel())
        }
        composable(NavigationItem.Main.route) {
            MainScreen(navController = navController, viewModel = hiltViewModel())
        }
//        composable(NavigationItem.Message.route) {
//            MessageScreen(navController)
//        }
        composable(NavigationItem.Account.route) {
            SetNicknameScreen(navController, hiltViewModel())
        }
    }
}