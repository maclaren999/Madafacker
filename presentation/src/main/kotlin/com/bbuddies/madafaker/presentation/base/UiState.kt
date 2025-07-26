package com.bbuddies.madafaker.presentation.base

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bbuddies.madafaker.presentation.R

sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val exception: Throwable, val message: String? = null) : UiState<Nothing>()
}

// Extension functions
fun <T> UiState<T>.getDataOrNull(): T? = when (this) {
    is UiState.Success -> data
    else -> null
}

val <T> UiState<T>.isLoading: Boolean get() = this is UiState.Loading
val <T> UiState<T>.isSuccess: Boolean get() = this is UiState.Success
val <T> UiState<T>.isError: Boolean get() = this is UiState.Error

@Composable
fun <T> UiState<T>.HandleState(
    onRetry: (() -> Unit)? = null,
    content: @Composable (T) -> Unit
) {
    when (this) {
        is UiState.Loading -> DefaultLoadingContent()
        is UiState.Success -> content(data)
        is UiState.Error -> DefaultErrorContent(
            message = message ?: exception.localizedMessage ?: stringResource(R.string.error_default),
            onRetry = onRetry
        )
    }
}

@Composable
private fun DefaultLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun DefaultErrorContent(
    message: String,
    onRetry: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )

        if (onRetry != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text(
                    text = stringResource(R.string.button_retry),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

// Helper functions to create UiState from operations
inline fun <T> uiStateOf(operation: () -> T): UiState<T> = try {
    UiState.Success(operation())
} catch (e: Exception) {
    UiState.Error(e)
}

suspend inline fun <T> suspendUiStateOf(crossinline operation: suspend () -> T): UiState<T> = try {
    UiState.Success(operation())
} catch (e: Exception) {
    UiState.Error(e)
}