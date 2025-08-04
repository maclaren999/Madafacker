package com.bbuddies.madafaker.presentation.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.Reply
import com.bbuddies.madafaker.presentation.DeepLinkData
import com.bbuddies.madafaker.presentation.NavigationItem
import com.bbuddies.madafaker.presentation.R
import com.bbuddies.madafaker.presentation.base.MovingSunEffect
import com.bbuddies.madafaker.presentation.base.ScreenWithWarnings
import com.bbuddies.madafaker.presentation.base.UiState
import com.bbuddies.madafaker.presentation.ui.main.tabs.AccountTab
import com.bbuddies.madafaker.presentation.ui.main.tabs.AccountTabViewModel
import com.bbuddies.madafaker.presentation.ui.main.tabs.InboxTab
import com.bbuddies.madafaker.presentation.ui.main.tabs.MyPostsTab
import com.bbuddies.madafaker.presentation.ui.main.tabs.WriteTab
import com.bbuddies.madafaker.presentation.utils.SharedTextManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    viewModel: MainScreenContract,
    modifier: Modifier = Modifier,
    deepLinkData: DeepLinkData? = null
) {
    val pagerState = rememberPagerState(pageCount = { MainTab.entries.size })
    val scope = rememberCoroutineScope()
    val highlightedMessageId by viewModel.highlightedMessageId.collectAsState()
    val currentMode by viewModel.currentMode.collectAsState()

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

    ScreenWithWarnings(
        warningsFlow = viewModel.warningsFlow,
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            MovingSunEffect(
                size = 64.dp,
                alignment = Alignment.TopStart,
                glowEnabled = true,
                padding = 24.dp
            )

            Image(
                painter = painterResource(id = R.drawable.blur_top_bar),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            MainScreenTabs(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp),
                pagerState = pagerState,
                scope = scope
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 72.dp)
                    .windowInsetsPadding(WindowInsets.systemBars)
            ) {
                // Add offline indicator at the top

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
                                onNavigateToAuth = {
                                    navController.navigate(NavigationItem.Account.route) {
                                        popUpTo(NavigationItem.Main.route) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


// Preview implementation
private class PreviewMainViewModel : MainScreenContract {
    private val _draftMessage = MutableStateFlow("Sample preview message")
    override val draftMessage: StateFlow<String> = _draftMessage

    private val _isSending = MutableStateFlow(false)
    override val isSending: StateFlow<Boolean> = _isSending

    private val _incomingMessages = MutableStateFlow<UiState<List<Message>>>(UiState.Loading)
    override val incomingMessages: StateFlow<UiState<List<Message>>> = _incomingMessages

    private val _outcomingMessages = MutableStateFlow<UiState<List<Message>>>(UiState.Loading)
    override val outcomingMessages: StateFlow<UiState<List<Message>>> = _outcomingMessages

    private val _currentMode = MutableStateFlow(Mode.SHINE)
    override val currentMode: StateFlow<Mode> = _currentMode

    private val _isReplySending = MutableStateFlow(false)
    override val isReplySending: StateFlow<Boolean> = _isReplySending

    private val _replyError = MutableStateFlow<String?>(null)
    override val replyError: StateFlow<String?> = _replyError

    private val _highlightedMessageId = MutableStateFlow<String?>(null)
    override val highlightedMessageId: StateFlow<String?> = _highlightedMessageId

    private val _replyingMessageId = MutableStateFlow<String?>(null)
    override val replyingMessageId: StateFlow<String?> = _replyingMessageId

    private val _userRepliesForMessage = MutableStateFlow<List<Reply>>(emptyList())
    override val userRepliesForMessage: StateFlow<List<Reply>> = _userRepliesForMessage

    private val _warningsFlow = MutableStateFlow<((android.content.Context) -> String?)?>(null)
    override val warningsFlow: StateFlow<((android.content.Context) -> String?)?> = _warningsFlow

    override val sharedTextManager = SharedTextManager()

    override fun onSendMessage(message: String) {}
    override fun onDraftMessageChanged(message: String) {
        _draftMessage.value = message
    }

    override fun toggleMode() {}
    override fun refreshMessages() {}
    override fun refreshUserData() {}
    override fun clearDraft() {}
    override fun onSendReply(messageId: String, replyText: String, isPublic: Boolean) {}
    override fun clearReplyError() {}
    override fun onRateMessage(messageId: String, rating: com.bbuddies.madafaker.common_domain.enums.MessageRating) {}
    override fun onInboxViewed() {}
    override fun markMessageAsRead(messageId: String) {}
    override fun onMessageTapped(messageId: String) {}
    override fun onMessageReplyingClosed() {}
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    MainScreen(
        navController = NavHostController(LocalContext.current),
        viewModel = PreviewMainViewModel()
    )
}