package com.bbuddies.madafaker.presentation.base

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bbuddies.madafaker.presentation.R
import kotlinx.coroutines.flow.StateFlow

@Composable
fun WarningSnackbarHost(
    warningsFlow: StateFlow<((context: Context) -> String?)?>
) {
    val context = LocalContext.current
    val warningMessage by warningsFlow.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(warningMessage) {
        warningMessage?.let { getMessage ->
            val message = getMessage(context) ?: context.getString(R.string.error_generic)
            snackbarHostState.showSnackbar(message)
        }
    }

    SnackbarHost(
        hostState = snackbarHostState
    ) { snackbarData ->
        Snackbar(
            snackbarData = snackbarData,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun ScreenWithWarnings(
    warningsFlow: StateFlow<((context: Context) -> String?)?>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        content()
        WarningSnackbarHost(warningsFlow = warningsFlow)
    }
}