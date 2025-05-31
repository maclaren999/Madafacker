package com.bbuddies.madafaker.presentation.ui.account

import android.content.Context
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bbuddies.madafaker.presentation.NavigationItem
import com.bbuddies.madafaker.presentation.base.MfResult
import com.bbuddies.madafaker.presentation.base.WarningSnackbarHost
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Preview
@Composable
fun SetNicknameScreenPreview() {
    SetNicknameScreen(
        draftNickname = "Nickname",
        validationResult = MfResult.Success(Unit),
        onNicknameChange = {},
        onDeleteAccount = {},
        onSaveNickname = {},
        warningsFlow = MutableStateFlow(null)
    )
}

@Composable
fun SetNicknameScreen(
    navController: NavController,
    viewModel: NewUserViewModel
) {
    val draftNickname by viewModel.draftNickname.collectAsState()
    val validationResult by viewModel.nicknameDraftValidationResult.collectAsState()

    SetNicknameScreen(
        draftNickname = draftNickname,
        validationResult = validationResult,
        onNicknameChange = { newDraft ->
            viewModel.onDraftNickChanged(newDraft)
        },
        onDeleteAccount = { viewModel.handleDeleteAccount() },
        onSaveNickname = {
            viewModel.onSaveNickname(onSuccessfulSave =
            { navController.navigate(NavigationItem.Main.route) })
        },
        warningsFlow = viewModel.warningsFlow
    )

}

@Composable
fun SetNicknameScreen(
    draftNickname: String,
    validationResult: MfResult<Unit>?,
    onNicknameChange: (String) -> Unit,
    onDeleteAccount: () -> Unit,
    onSaveNickname: () -> Unit,
    warningsFlow: StateFlow<((context: Context) -> String?)?>
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

        NicknameInputBlock(
            draftNickname,
            validationResult,
            onNicknameChange = onNicknameChange,
            onSaveNickname = onSaveNickname
        )
        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(16.dp))
        DeleteAccountButton(onDeleteAccount)
    }

    WarningSnackbarHost(warningsFlow = warningsFlow)
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
fun NicknameInputBlock(
    draftNickname: String,
    validationResult: MfResult<Unit>?,
    onNicknameChange: (String) -> Unit,
    onSaveNickname: () -> Unit,
) {
    Column {
        TextField(
            value = draftNickname,
            onValueChange = {
                onNicknameChange(it)
            },
            label = { Text(draftNickname) },
            trailingIcon = {
                Text(
                    text = "${draftNickname.length}/100",
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
            supportingText = {
                when (validationResult) {
                    is MfResult.Loading -> Text("Validating nickname...", color = Color.Gray)
                    is MfResult.Error -> Text(
                        validationResult.getErrorString.invoke(LocalContext.current),
                        color = Color.Red
                    )

                    else -> {}
                }
            }
        )
        Button(
            onClick = { onSaveNickname() },
            enabled = validationResult is MfResult.Success,
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 8.dp)
        ) {
            Text("Save Nickname")
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

