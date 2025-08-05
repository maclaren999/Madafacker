package com.bbuddies.madafaker.presentation.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import com.bbuddies.madafaker.common_domain.model.DeepLinkData
import com.bbuddies.madafaker.presentation.navigation.actions.MainNavigationAction
import com.bbuddies.madafaker.presentation.ui.main.tabs.AccountTab
import com.bbuddies.madafaker.presentation.ui.main.tabs.AccountTabViewModel
import com.bbuddies.madafaker.presentation.ui.main.tabs.InboxTab
import com.bbuddies.madafaker.presentation.ui.main.tabs.MyPostsTab
import com.bbuddies.madafaker.presentation.ui.main.tabs.WriteTab
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabNavigationScreen(
    navAction: MainNavigationAction,
    viewModel: MainScreenContract,
    modifier: Modifier = Modifier,
    deepLinkData: DeepLinkData? = null
) {
    val pagerState = rememberPagerState(pageCount = { MainTab.entries.size })
    val scope = rememberCoroutineScope()
    val highlightedMessageId by viewModel.highlightedMessageId.collectAsState()

    // Pull-to-refresh state
    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    // Function to handle refresh with proper state management
    val handleRefresh: () -> Unit = remember {
        {
            scope.launch {
                if (!isRefreshing) {
                    isRefreshing = true
                    try {
                        // Refresh both messages and user data for all tabs
                        viewModel.refreshMessages()
                        viewModel.refreshUserData()

                        // Minimum delay to ensure smooth animation
                        delay(600)
                    } catch (e: Exception) {
                        // Handle errors gracefully with shorter delay
                        delay(400)
                    }
                    // Always reset the state
                    isRefreshing = false
                }
            }
        }
    }

    // Handle deep link navigation to Inbox tab
    LaunchedEffect(deepLinkData) {
        if (deepLinkData != null) {
            // Navigate to Inbox tab (index 2)
            pagerState.animateScrollToPage(MainTab.INBOX.ordinal)
        }
    }

    // Handle organic navigation to Inbox tab
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == MainTab.INBOX.ordinal && deepLinkData == null) {
            // User navigated to inbox organically (not via notification)
            viewModel.onInboxViewed()
        }
    }

    // Handle shared text navigation
    val hasUnconsumedSharedText by viewModel.sharedTextManager.hasUnconsumedSharedText.collectAsState()

    // Navigate to WriteTab when shared text is received
    LaunchedEffect(hasUnconsumedSharedText) {
        if (hasUnconsumedSharedText && pagerState.currentPage != MainTab.WRITE.ordinal) {
            scope.launch {
                pagerState.animateScrollToPage(MainTab.WRITE.ordinal)
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
            currentTab = MainTab.entries[pagerState.currentPage],
            onTabSelected = { tab ->
                scope.launch {
                    pagerState.animateScrollToPage(tab.ordinal)
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
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (MainTab.entries[page]) {
                    MainTab.WRITE -> WriteTab(viewModel)
                    MainTab.MY_POSTS -> MyPostsTab(viewModel)
                    MainTab.INBOX -> InboxTab(
                        viewModel = viewModel,
                        highlightedMessageId = highlightedMessageId
                    )
                    MainTab.ACCOUNT -> AccountTab(
                        viewModel = hiltViewModel<AccountTabViewModel>(),
                        onNavigateToAuth = { navAction.navigateToAuthFromMain() }
                    )
                }
            }
        }
    }
}
