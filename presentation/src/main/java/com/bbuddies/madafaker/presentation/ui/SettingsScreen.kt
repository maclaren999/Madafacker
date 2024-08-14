package com.bbuddies.madafaker.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(SettingsViewModel())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val nickname by viewModel.nickname.collectAsState()
    val fullName by viewModel.fullName.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        // Profile Section
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        ) {
            // Placeholder for avatar
            Text(
                text = fullName.firstOrNull()?.toString() ?: "A",
                modifier = Modifier.align(Alignment.Center),
                fontSize = 32.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = fullName,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Nickname Input
        TextField(
            value = nickname,
            onValueChange = { viewModel.updateNickname(it) },
            label = { Text("Nickname") },
            trailingIcon = {
                Text(
                    text = "${nickname.length}/100",
                    modifier = Modifier.padding(end = 8.dp),
                    color = Color.Gray
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x00AFAFAF), shape = RoundedCornerShape(8.dp)),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Log out button
        Row(
            modifier = Modifier
                .background(color = Color(0xFFFBFAFA), shape = RoundedCornerShape(size = 8.dp)),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = { viewModel.logOut() },
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Black),
                modifier = Modifier
                    .padding(16.dp)
                    .width(280.dp)
                    .height(48.dp)
            ) {
                Text("Log out")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Delete account button
        Row(
            modifier = Modifier
                .background(color = Color(0xFFFBFAFA), shape = RoundedCornerShape(size = 8.dp)),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = { viewModel.deleteAccount() },
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Black),
                modifier = Modifier
                    .padding(
                        start = 28.dp, top = 16.dp, end = 28.dp, bottom = 16.dp
                    )
                    .width(328.dp)
                    .height(60.dp)
            ) {
                Text("Delete account")
            }
        }
    }
}