package com.bbuddies.madafaker.presentation

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.DeepLinkData
import com.bbuddies.madafaker.presentation.design.components.ModeBackground
import com.bbuddies.madafaker.presentation.ui.auth.AuthNavigationAction
import com.bbuddies.madafaker.presentation.ui.auth.AuthScreen
import com.bbuddies.madafaker.presentation.ui.main.MainTab
import com.bbuddies.madafaker.presentation.ui.main.MainViewModel
import com.bbuddies.madafaker.presentation.ui.main.tabs.AccountTab
import com.bbuddies.madafaker.presentation.ui.main.tabs.AccountTabViewModel
import com.bbuddies.madafaker.presentation.ui.main.tabs.inbox.InboxTab
import com.bbuddies.madafaker.presentation.ui.main.tabs.inbox.InboxTabViewModel
import com.bbuddies.madafaker.presentation.ui.main.tabs.myposts.MyPostsTab
import com.bbuddies.madafaker.presentation.ui.main.tabs.myposts.MyPostsTabViewModel
import com.bbuddies.madafaker.presentation.ui.main.tabs.write.WriteTab
import com.bbuddies.madafaker.presentation.ui.main.tabs.write.WriteTabViewModel
import com.bbuddies.madafaker.presentation.ui.navigation.NavigationVisibility
import com.bbuddies.madafaker.presentation.ui.navigation.TopLevelDestination
import com.bbuddies.madafaker.presentation.ui.navigation.TopNavigationBar
import com.bbuddies.madafaker.presentation.ui.navigation.navigateToTopLevelDestination
import com.bbuddies.madafaker.presentation.ui.navigation.toTopLevelDestination
import com.bbuddies.madafaker.presentation.ui.navigation.topLevelDestinations
import com.bbuddies.madafaker.presentation.ui.permission.NotificationPermissionNavigationAction
import com.bbuddies.madafaker.presentation.ui.permission.NotificationPermissionScreen
import com.bbuddies.madafaker.presentation.ui.splash.SplashNavigationAction
import com.bbuddies.madafaker.presentation.ui.splash.SplashScreen
import kotlinx.serialization.Serializable

// ============================================================================
// TYPE-SAFE ROUTES using Kotlin Serialization
// ============================================================================

@Serializable
object SplashRoute

@Serializable
object AuthRoute

@Serializable
data class AuthWithRedirectRoute(
    val redirectRoute: String? = null
)

@Serializable
object NotificationPermissionRoute

// ============================================================================
// TAB ROUTES for unified navigation
// ============================================================================

@Serializable
object WriteTabRoute

@Serializable
object MyPostsTabRoute

@Serializable
object InboxTabRoute

@Serializable
object AccountTabRoute

@Serializable
data class InboxTabWithDeepLinkRoute(
    val messageId: String,
    val notificationId: String,
    val mode: Mode
)

// ============================================================================
// TOP LEVEL DESTINATIONS & NAVIGATION VISIBILITY
// ============================================================================

/**
 * Top-level destinations and navigation visibility logic are defined in NavDisplay.kt
 *
 * TopLevelDestination Pattern provides:
 * - Centralized navigation logic
 * - Proper back stack management
 * - State preservation across tab switches
 * - Type-safe navigation
 *
 * NavigationVisibility provides:
 * - Type-safe route checking for navigation visibility
 * - Centralized logic for showing/hiding navigation
 * - Easy to extend and maintain
 *
 * Usage:
 * - Use topLevelDestinations list to iterate over all tabs
 * - Use navigateToTopLevelDestination() extension for navigation
 * - Use toTopLevelDestination() to convert MainTab to TopLevelDestination
 * - Use shouldShowTopNavigation() to check if navigation should be visible
 * - Use isAtTopLevelDestination() to check if at a top-level destination
 */

/**
 * Main App Navigation Host
 * Sets up navigation graph and handles deep links and shared text
 */

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: Any = SplashRoute,
    deepLinkData: DeepLinkData? = null
) {
    val mainViewModel: MainViewModel = hiltViewModel()

    // Handle deep link navigation
    LaunchedEffect(deepLinkData) {
        if (deepLinkData != null && deepLinkData.isValid()) {
            navController.navigate(
                InboxTabWithDeepLinkRoute(
                    messageId = deepLinkData.messageId,
                    notificationId = deepLinkData.notificationId,
                    mode = deepLinkData.mode
                )
            )
        }
    }

    // Handle shared text navigation using topLevelDestination
    LaunchedEffect(Unit) {
        mainViewModel.sharedTextManager.hasUnconsumedSharedText.collect { hasSharedText ->
            if (hasSharedText) {
                mainViewModel.selectTab(MainTab.WRITE)
                val writeDestination = MainTab.WRITE.toTopLevelDestination()
                navController.navigateToTopLevelDestination(writeDestination)
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val shouldShowNavigation = NavigationVisibility.shouldShowNavigation(currentRoute)
    val currentIndex = topLevelDestinations.indexOfFirst { destination ->
        currentRoute?.contains(destination.route::class.simpleName ?: "") == true
    }.coerceAtLeast(0)
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = 64f

    fun navigateTo(destination: TopLevelDestination) {
        mainViewModel.selectTab(destination.tab)
        navController.navigateToTopLevelDestination(destination)
    }

    // Get current mode from MainViewModel
    val currentMode by mainViewModel.currentMode.collectAsState()
    val hasSeenModeToggleTip by mainViewModel.hasSeenModeToggleTip.collectAsState()

    ModeBackground(
        mode = currentMode,
        showDecorative = shouldShowNavigation,
        showModeTip = shouldShowNavigation && !hasSeenModeToggleTip,
        onModeToggle = { mainViewModel.toggleMode() },
        onTooltipDismissed = { mainViewModel.onModeToggleTipDismissed() }
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .pointerInput(currentRoute) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            dragOffset += dragAmount
                        },
                        onDragEnd = {
                            if (NavigationVisibility.isTopLevelDestination(currentRoute)) {
                                when {
                                    dragOffset > swipeThreshold && currentIndex > 0 -> {
                                        navigateTo(topLevelDestinations[currentIndex - 1])
                                    }

                                    dragOffset < -swipeThreshold && currentIndex < topLevelDestinations.lastIndex -> {
                                        navigateTo(topLevelDestinations[currentIndex + 1])
                                    }
                                }
                            }
                            dragOffset = 0f
                        },
                        onDragCancel = { dragOffset = 0f }
                    )
                }
        ) {
            // Navigation with custom design
            Scaffold(
                containerColor = Color.Transparent,
            ) { paddingValues ->
                Column(
                    modifier = Modifier.padding(paddingValues)
                ) {
                    // Custom top navigation - shows/hides based on current route
                    // Recomposes when navBackStackEntry changes
                    if (shouldShowNavigation) {
                        TopNavigationBar(
                            navController = navController,
                            mode = currentMode,
                            onTabSelected = { tab -> mainViewModel.selectTab(tab) }
                        )
                    }

                    // Main content
                    NavHost(
                        modifier = Modifier.fillMaxSize(),
                        navController = navController,
                        startDestination = if (startDestination == SplashRoute) SplashRoute else WriteTabRoute
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


                        // Auth Screen (simple)
                        composable<AuthRoute> {
                            val authNavAction = AuthNavigationAction(navController)

                            AuthScreen(
                                navAction = authNavAction,
                                viewModel = hiltViewModel(),
                                redirectRoute = null,
                                modifier = Modifier
                                    .fillMaxSize()
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
                            val permissionNavAction =
                                NotificationPermissionNavigationAction(navController)

                            NotificationPermissionScreen(
                                navAction = permissionNavAction,
                                viewModel = hiltViewModel(),
                                modifier = Modifier
                                    .fillMaxSize()
                            )
                        }

                        // ============================================================================
                        // TAB SCREENS (unified navigation)
                        // ============================================================================

                        // Write Tab
                        composable<WriteTabRoute> {
                            val writeTabViewModel: WriteTabViewModel = hiltViewModel()
                            val writeTabState by writeTabViewModel.state.collectAsState()
                            WriteTab(
                                state = writeTabState,
                                warningsFlow = writeTabViewModel.warningsFlow,
                                onDraftMessageChanged = writeTabViewModel::onDraftMessageChanged,
                                onSendMessage = writeTabViewModel::onSendMessage
                            )
                        }

                        // My Posts Tab
                        composable<MyPostsTabRoute> {
                            val myPostsTabViewModel: MyPostsTabViewModel = hiltViewModel()
                            val myPostsState by myPostsTabViewModel.state.collectAsState()
                            MyPostsTab(
                                state = myPostsState,
                                onRefreshMessages = myPostsTabViewModel::refreshMessages
                            )
                        }

                        // Inbox Tab
                        composable<InboxTabRoute> {
                            val inboxTabViewModel: InboxTabViewModel = hiltViewModel()
                            val inboxState by inboxTabViewModel.state.collectAsState()
                            InboxTab(
                                state = inboxState,
                                highlightedMessageId = null,
                                onInboxViewed = inboxTabViewModel::onInboxViewed,
                                onRefreshMessages = inboxTabViewModel::refreshMessages,
                                onSendReply = inboxTabViewModel::onSendReply,
                                onReplyingClosed = inboxTabViewModel::onMessageReplyingClosed,
                                onRateMessage = inboxTabViewModel::onRateMessage,
                                onMessageTapped = inboxTabViewModel::onMessageTapped,
                                onMarkMessageRead = inboxTabViewModel::markMessageAsRead,
                                onSnackbarConsumed = inboxTabViewModel::clearInboxSnackbar
                            )
                        }

                        // Inbox Tab with Deep Link - shares ViewModel with regular InboxTab via navigation graph
                        composable<InboxTabWithDeepLinkRoute> { backStackEntry ->
                            val route = backStackEntry.toRoute<InboxTabWithDeepLinkRoute>()
                            val inboxTabViewModel: InboxTabViewModel = hiltViewModel()
                            val inboxState by inboxTabViewModel.state.collectAsState()

                            // Set highlighted message from deep link
                            LaunchedEffect(route.messageId) {
                                inboxTabViewModel.setHighlightedMessage(route.messageId)
                            }

                            InboxTab(
                                state = inboxState,
                                highlightedMessageId = route.messageId,
                                onInboxViewed = inboxTabViewModel::onInboxViewed,
                                onRefreshMessages = inboxTabViewModel::refreshMessages,
                                onSendReply = inboxTabViewModel::onSendReply,
                                onReplyingClosed = inboxTabViewModel::onMessageReplyingClosed,
                                onRateMessage = inboxTabViewModel::onRateMessage,
                                onMessageTapped = inboxTabViewModel::onMessageTapped,
                                onMarkMessageRead = inboxTabViewModel::markMessageAsRead,
                                onSnackbarConsumed = inboxTabViewModel::clearInboxSnackbar
                            )
                        }

                        // Account Tab
                        composable<AccountTabRoute> {
                            val accountViewModel: AccountTabViewModel = hiltViewModel()
                            val accountState by accountViewModel.state.collectAsState()

                            AccountTab(
                                state = accountState,
                                warningsFlow = accountViewModel.warningsFlow,
                                onRefreshNotificationPermission = accountViewModel::refreshNotificationPermission,
                                onDeleteAccountClick = accountViewModel::onDeleteAccountClick,
                                onLogoutClick = accountViewModel::onLogoutClick,
                                onFeedbackClick = accountViewModel::onFeedbackClick,
                                onSendDeleteAccountEmail = accountViewModel::sendDeleteAccountEmail,
                                onPerformLogout = {
                                    accountViewModel.performLogout {
                                        navController.navigate(AuthRoute) {
                                            popUpTo(WriteTabRoute) { inclusive = true }
                                        }
                                    }
                                },
                                onDismissDeleteAccountDialog = accountViewModel::dismissDeleteAccountDialog,
                                onDismissLogoutDialog = accountViewModel::dismissLogoutDialog,
                                onFeedbackTextChange = accountViewModel::onFeedbackTextChange,
                                onRatingChange = accountViewModel::onRatingChange,
                                onSubmitFeedback = accountViewModel::submitFeedback,
                                onDismissFeedbackDialog = accountViewModel::dismissFeedbackDialog
                            )
                        }
                    }
                }
            }

        }
    }
}
