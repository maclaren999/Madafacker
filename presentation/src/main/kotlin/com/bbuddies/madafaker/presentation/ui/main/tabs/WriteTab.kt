package com.bbuddies.madafaker.presentation.ui.main.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bbuddies.madafaker.presentation.ui.main.MainScreenContract
import com.bbuddies.madafaker.presentation.ui.main.SendMessageView
import com.bbuddies.madafaker.presentation.ui.main.components.OfflineIndicator
import com.bbuddies.madafaker.presentation.ui.main.components.RetryPendingMessagesButton

@Composable
fun WriteTab(viewModel: MainScreenContract) {
    val hasPendingMessages by viewModel.hasPendingMessages.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Show retry button when there are pending messages and we're online
        if (hasPendingMessages && isOnline) {
            RetryPendingMessagesButton(
                onClick = { viewModel.retryPendingMessages() },
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }

        OfflineIndicator(
            isOnline = isOnline,
            hasPendingMessages = hasPendingMessages,
            modifier = Modifier.padding(16.dp)
        )

        SendMessageView(viewModel)
    }
}