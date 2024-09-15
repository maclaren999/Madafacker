package com.bbuddies.madafaker.presentation.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController


@Preview
@Composable
fun MainScreenPreview() {
}
@Composable
fun MainScreen(
    navController: NavHostController,
    viewModel: MainViewModel
) {
    var text by rememberSaveable { mutableStateOf("") }
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        SendMessageView(viewModel)
    }
}

@Composable
fun SendMessageView(viewModel: MainViewModel) {
    val draftMessage by viewModel.draftMessage.collectAsState()

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        OutlinedTextField(
            value = draftMessage,
            modifier = Modifier
                .height(400.dp)
                .fillMaxWidth()
                .padding(16.dp),
            onValueChange = { newValue -> viewModel.onSendMessage(newValue) },
            label = { Text(text = "Express Yourself") }
        )
    }
}