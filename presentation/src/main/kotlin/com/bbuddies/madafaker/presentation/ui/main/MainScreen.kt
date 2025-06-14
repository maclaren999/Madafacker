package com.bbuddies.madafaker.presentation.ui.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.presentation.base.UiState
import com.bbuddies.madafaker.presentation.ui.main.components.OfflineIndicator
import com.bbuddies.madafaker.presentation.ui.main.tabs.AccountTab
import com.bbuddies.madafaker.presentation.ui.main.tabs.InboxTab
import com.bbuddies.madafaker.presentation.ui.main.tabs.MyPostsTab
import com.bbuddies.madafaker.presentation.ui.main.tabs.WriteTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun MainScreen(
    navController: NavHostController,
    viewModel: MainScreenContract,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { MainTab.entries.size })
    val scope = rememberCoroutineScope()
    val isOnline by viewModel.isOnline.collectAsState()
    val hasPendingMessages by viewModel.hasPendingMessages.collectAsState()

    Surface(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(MainScreenTheme.SunTop, MainScreenTheme.SunBottom)
                    )
                )
        ) {
            // Glowing sun at the top
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .align(Alignment.TopCenter)
            ) {
                val radius = size.width * 0.6f
                drawCircle(
                    color = MainScreenTheme.SunBody,
                    radius = radius,
                    center = Offset(x = size.width / 2, y = size.height * 1.2f)
                )
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // Add offline indicator at the top
                OfflineIndicator(
                    isOnline = isOnline,
                    hasPendingMessages = hasPendingMessages,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                MainScreenTabs(
                    pagerState = pagerState,
                    scope = scope
                )

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (MainTab.entries[page]) {
                        MainTab.WRITE -> WriteTab(viewModel)
                        MainTab.MY_POSTS -> MyPostsTab(viewModel)
                        MainTab.INBOX -> InboxTab(viewModel)
                        MainTab.ACCOUNT -> AccountTab()
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

    private val _isOnline = MutableStateFlow(true)
    override val isOnline: StateFlow<Boolean> = _isOnline

    private val _hasPendingMessages = MutableStateFlow(false)
    override val hasPendingMessages: StateFlow<Boolean> = _hasPendingMessages

    override fun onSendMessage(message: String) {}
    override fun onDraftMessageChanged(message: String) {
        _draftMessage.value = message
    }
    override fun toggleMode() {}
    override fun refreshMessages() {}
    override fun retryPendingMessages() {}
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    MainScreen(
        navController = NavHostController(LocalContext.current),
        viewModel = PreviewMainViewModel()
    )
}