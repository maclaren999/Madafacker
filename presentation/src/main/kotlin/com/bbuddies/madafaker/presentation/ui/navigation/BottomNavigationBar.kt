package com.bbuddies.madafaker.presentation.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.bbuddies.madafaker.presentation.AccountTabRoute
import com.bbuddies.madafaker.presentation.InboxTabRoute
import com.bbuddies.madafaker.presentation.MyPostsTabRoute
import com.bbuddies.madafaker.presentation.WriteTabRoute
import com.bbuddies.madafaker.presentation.ui.main.MainTab
import kotlin.reflect.KClass

/**
 * Bottom Navigation Bar component for unified tab navigation
 */
@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    onTabSelected: (MainTab) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val items = listOf(
        BottomNavItem(MainTab.WRITE, WriteTabRoute::class, "write_tab"),
        BottomNavItem(MainTab.MY_POSTS, MyPostsTabRoute::class, "my_posts_tab"),
        BottomNavItem(MainTab.INBOX, InboxTabRoute::class, "inbox_tab"),
        BottomNavItem(MainTab.ACCOUNT, AccountTabRoute::class, "account_tab")
    )
    
    NavigationBar {
        items.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any { 
                it.route?.contains(item.routeClass.simpleName ?: "") == true 
            } == true
            
            NavigationBarItem(
                icon = { 
                    Icon(
                        painter = painterResource(id = getTabIcon(item.tab)),
                        contentDescription = null
                    )
                },
                label = { 
                    Text(stringResource(item.tab.titleRes))
                },
                selected = isSelected,
                onClick = {
                    onTabSelected(item.tab)
                    when (item.tab) {
                        MainTab.WRITE -> navController.navigate(WriteTabRoute) {
                            popUpTo(WriteTabRoute) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                        MainTab.MY_POSTS -> navController.navigate(MyPostsTabRoute) {
                            popUpTo(WriteTabRoute) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                        MainTab.INBOX -> navController.navigate(InboxTabRoute) {
                            popUpTo(WriteTabRoute) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                        MainTab.ACCOUNT -> navController.navigate(AccountTabRoute) {
                            popUpTo(WriteTabRoute) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

/**
 * Data class for bottom navigation items
 */
data class BottomNavItem(
    val tab: MainTab,
    val routeClass: KClass<*>,
    val routeName: String
)

/**
 * Helper function to get tab icons
 * TODO: Replace with actual icon resources
 */
private fun getTabIcon(tab: MainTab): Int {
    return when (tab) {
        MainTab.WRITE -> android.R.drawable.ic_menu_edit
        MainTab.MY_POSTS -> android.R.drawable.ic_menu_view
        MainTab.INBOX -> android.R.drawable.ic_dialog_email
        MainTab.ACCOUNT -> android.R.drawable.ic_menu_myplaces
    }
}

/**
 * Helper function to check if we should show bottom navigation
 */
fun shouldShowBottomNavigation(navController: NavHostController): Boolean {
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    return currentRoute?.contains("TabRoute") == true ||
           currentRoute?.contains("WriteTabRoute") == true ||
           currentRoute?.contains("MyPostsTabRoute") == true ||
           currentRoute?.contains("InboxTabRoute") == true ||
           currentRoute?.contains("AccountTabRoute") == true
}
