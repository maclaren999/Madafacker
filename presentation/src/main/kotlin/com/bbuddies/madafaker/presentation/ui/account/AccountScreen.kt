package com.bbuddies.madafaker.presentation.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Preview
@Composable
fun AccountScreenPreview() {
    AccountScreen(
        nickname = "Preview User",
        onNicknameChange = {},
        onDeleteAccount = {},
    )
}

@Composable
fun AccountScreen(
    navController: NavController,
    viewModel: AccountViewModel
) {

    AccountScreen(
        nickname = nickname,
        onNicknameChange = { viewModel.onDraftNickChanged(it) },
        onDeleteAccount = { viewModel.deleteAccount() },
    )
}

@Composable
fun AccountScreen(
    nickname: String,
    onNicknameChange: (String) -> Unit,
    onDeleteAccount: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))
        ProfileAvatar()
        Spacer(modifier = Modifier.height(32.dp))

        NicknameInput(nickname, onNicknameChange)
        Spacer(modifier = Modifier.weight(1f))
//        LogoutButton(onLogout)
        Spacer(modifier = Modifier.height(16.dp))
        DeleteAccountButton(onDeleteAccount)
    }
}

@Composable
fun ProfileAvatar() {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        // Placeholder for avatar
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NicknameInput(nickname: String, onNicknameChange: (String) -> Unit) {
    TextField(
        value = nickname,
        onValueChange = onNicknameChange,
        label = { Text(nickname) },
        trailingIcon = {
            Text(
                text = "${nickname.length}/100",
                modifier = Modifier.padding(end = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        textStyle = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .padding(vertical = 16.dp)
            .fillMaxWidth()
            .height(64.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(8.dp),
    )
}

@Composable
fun LogoutButton(onLogout: () -> Unit) {
    Row(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(size = 8.dp)),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            onClick = onLogout,
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier
                .padding(
                    start = 28.dp, top = 16.dp, end = 28.dp, bottom = 16.dp
                )
                .width(328.dp)
                .height(60.dp)
        ) {
            Text("Log out")
        }
    }
}

@Composable
fun DeleteAccountButton(onDeleteAccount: () -> Unit) {
    Row(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(size = 8.dp)),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            onClick = onDeleteAccount,
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
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