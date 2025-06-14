package com.bbuddies.madafaker.presentation.ui.main.tabs

import androidx.compose.runtime.Composable
import com.bbuddies.madafaker.presentation.ui.main.MainScreenContract
import com.bbuddies.madafaker.presentation.ui.main.SendMessageView

@Composable
fun WriteTab(viewModel: MainScreenContract) {
    SendMessageView(viewModel)
}