package com.bbuddies.madafaker.presentation.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bbuddies.madafaker.common_domain.model.DeepLinkData
import com.bbuddies.madafaker.presentation.navigation.actions.MainNavigationAction
import com.bbuddies.madafaker.presentation.ui.main.tabs.AccountTab
import com.bbuddies.madafaker.presentation.ui.main.tabs.AccountTabViewModel
import com.bbuddies.madafaker.presentation.ui.main.tabs.InboxTab
import com.bbuddies.madafaker.presentation.ui.main.tabs.MyPostsTab
import com.bbuddies.madafaker.presentation.ui.main.tabs.WriteTab
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Tab routes for navigation
const val WRITE_TAB_ROUTE = "write_tab"
const val FEED_TAB_ROUTE = "feed_tab"
const val INBOX_TAB_ROUTE = "inbox_tab"
const val ACCOUNT_TAB_ROUTE = "account_tab"

/**
 * Alternative TabNavigationScreen using Navigation Compose
 * This version provides better separation of concerns and more standard navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabNavigationScreenV2(
    navAction: MainNavigationAction,
    viewModel: MainScreenContract,
    modifier: Modifier = Modifier,
    deepLinkData: DeepLinkData? = null
) {
    val tabNavController = rememberNavController()
    val tabNavigationViewModel: TabNavigationViewModel = hiltViewModel()
    val currentTab by tabNavigationViewModel.currentTab.collectAsState()
    val highlightedMessageId by viewModel.highlightedMessageId.collectAsState()
    val scope = rememberCoroutineScope()

    // Pull-to-refresh state
    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    // Function to handle refresh
    val handleRefresh: () -> Unit = remember {
        {
            if (!isRefreshing) {
                isRefreshing = true
                scope.launch {
                    try {
                        viewModel.refreshMessages()
                        viewModel.refreshUserData()
                        delay(600)
                    } catch (e: Exception) {
                        delay(400)
                    }
                    isRefreshing = false
                }
            }
        }
    }

    // Handle deep link navigation
    LaunchedEffect(deepLinkData) {
        if (deepLinkData != null) {
            tabNavigationViewModel.selectTab(MainTab.INBOX)
            tabNavController.navigate(INBOX_TAB_ROUTE) {
                popUpTo(tabNavController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    // Handle shared text navigation
    val hasUnconsumedSharedText by viewModel.sharedTextManager.hasUnconsumedSharedText.collectAsState()
    LaunchedEffect(hasUnconsumedSharedText) {
        if (hasUnconsumedSharedText && currentTab != MainTab.WRITE) {
            tabNavigationViewModel.selectTab(MainTab.WRITE)
            tabNavController.navigate(WRITE_TAB_ROUTE) {
                popUpTo(tabNavController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 72.dp)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        // Tab navigation
        TabBar(
            currentTab = currentTab,
            onTabSelected = { tab ->
                tabNavigationViewModel.selectTab(tab)
                val route = when (tab) {
                    MainTab.WRITE -> WRITE_TAB_ROUTE
                    MainTab.MY_POSTS -> FEED_TAB_ROUTE
                    MainTab.INBOX -> INBOX_TAB_ROUTE
                    MainTab.ACCOUNT -> ACCOUNT_TAB_ROUTE
                }
                tabNavController.navigate(route) {
                    popUpTo(tabNavController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Tab content with pull-to-refresh
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = handleRefresh,
            state = pullToRefreshState
        ) {
            TabNavHost(
                navController = tabNavController,
                viewModel = viewModel,
                navAction = navAction,
                highlightedMessageId = highlightedMessageId,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun TabNavHost(
    navController: NavHostController,
    viewModel: MainScreenContract,
    navAction: MainNavigationAction,
    highlightedMessageId: String?,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = WRITE_TAB_ROUTE,
        modifier = modifier
    ) {
        composable(WRITE_TAB_ROUTE) {
            WriteTab(viewModel)
        }
        
        composable(FEED_TAB_ROUTE) {
            MyPostsTab(viewModel)
        }
        
        composable(INBOX_TAB_ROUTE) {
            InboxTab(
                viewModel = viewModel,
                highlightedMessageId = highlightedMessageId
            )
        }
        
        composable(ACCOUNT_TAB_ROUTE) {
            AccountTab(
                viewModel = hiltViewModel<AccountTabViewModel>(),
                onNavigateToAuth = { navAction.navigateToAuthFromMain() }
            )
        }
    }
}
