package com.bbuddies.madafaker.presentation.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.DeepLinkData
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.Reply
import com.bbuddies.madafaker.presentation.R
import com.bbuddies.madafaker.presentation.navigation.actions.MainNavigationAction
import com.bbuddies.madafaker.presentation.base.MovingSunEffect
import com.bbuddies.madafaker.presentation.base.ScreenWithWarnings
import com.bbuddies.madafaker.presentation.base.UiState
import com.bbuddies.madafaker.presentation.utils.SharedTextManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navAction: MainNavigationAction,
    viewModel: MainScreenContract,
    modifier: Modifier = Modifier,
    deepLinkData: DeepLinkData? = null
) {
    val currentMode by viewModel.currentMode.collectAsState()



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

            // Use the new TabNavigationScreen
            TabNavigationScreen(
                navAction = navAction,
                viewModel = viewModel,
                deepLinkData = deepLinkData,
                modifier = Modifier.fillMaxSize()
            )
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
    // Create a mock navigation action for preview
    val mockNavController = androidx.navigation.NavHostController(LocalContext.current)
    val mockNavAction = MainNavigationAction(mockNavController)

    MainScreen(
        navAction = mockNavAction,
        viewModel = PreviewMainViewModel()
    )
}