package com.bbuddies.madafaker.presentation.ui.main.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bbuddies.madafaker.presentation.ui.main.MainScreenContract
import com.bbuddies.madafaker.presentation.ui.main.SendMessageView
import com.bbuddies.madafaker.presentation.ui.main.components.OfflineIndicator

@Composable
fun WriteTab(viewModel: MainScreenContract) {
    val hasPendingMessages by viewModel.hasPendingMessages.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OfflineIndicator(
            isOnline = isOnline,
            hasPendingMessages = hasPendingMessages,
            modifier = Modifier.padding(16.dp)
        )

        SendMessageView(viewModel)
    }
}