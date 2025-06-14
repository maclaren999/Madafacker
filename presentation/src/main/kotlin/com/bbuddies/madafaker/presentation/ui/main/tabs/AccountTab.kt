package com.bbuddies.madafaker.presentation.ui.main.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.bbuddies.madafaker.presentation.R
import com.bbuddies.madafaker.presentation.ui.main.MainScreenTheme

@Composable
fun AccountTab() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.account_settings_coming_soon),
            color = MainScreenTheme.TextPrimary
        )
    }
}