package com.bbuddies.madafaker.presentation.ui.account

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bbuddies.madafaker.presentation.NavigationItem
import com.bbuddies.madafaker.presentation.base.MfResult
import com.bbuddies.madafaker.presentation.base.ScreenWithWarnings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Preview
@Composable
fun AuthScreenPreview() {
    AuthScreen(
        authUiState = AuthUiState.INITIAL,
        draftNickname = "Nickname",
        validationResult = MfResult.Success(Unit),
        onNicknameChange = {},
        onGoogleSignIn = {},
        onCreateAccount = {},
        warningsFlow = MutableStateFlow(null),
        isSigningIn = false
    )
}

@Composable
fun AuthScreen(
    navController: NavController,
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val authUiState by viewModel.authUiState.collectAsState()
    val draftNickname by viewModel.draftNickname.collectAsState()
    val validationResult by viewModel.nicknameDraftValidationResult.collectAsState()
    val isSigningIn by viewModel.isSigningIn.collectAsState()

    AuthScreen(
        authUiState = authUiState,
        draftNickname = draftNickname,
        validationResult = validationResult,
        onNicknameChange = { newDraft ->
            viewModel.onDraftNickChanged(newDraft)
        },
        onGoogleSignIn = {
            viewModel.onGoogleSignIn(
                onSuccessfulSignIn = { notificationPermissionHelper ->
                    // Check if notification permission is already granted
                    val nextDestination = if (notificationPermissionHelper.isNotificationPermissionGranted()) {
                        NavigationItem.Main
                    } else {
                        NavigationItem.NotificationPermission
                    }

                    navController.navigate(nextDestination.route) {
                        popUpTo(NavigationItem.Account.route) { inclusive = true }
                    }
                }
            )
        },
        onCreateAccount = {
            viewModel.onCreateAccount(
                onSuccessfulCreation = { notificationPermissionHelper ->
                    // Check if notification permission is already granted
                    val nextDestination = if (notificationPermissionHelper.isNotificationPermissionGranted()) {
                        NavigationItem.Main
                    } else {
                        NavigationItem.NotificationPermission
                    }

                    navController.navigate(nextDestination.route) {
                        popUpTo(NavigationItem.Account.route) { inclusive = true }
                    }
                }
            )
        },
        warningsFlow = viewModel.warningsFlow,
        isSigningIn = isSigningIn,
        modifier = modifier
    )
}

@Composable
fun AuthScreen(
    authUiState: AuthUiState,
    draftNickname: String,
    validationResult: MfResult<Unit>?,
    onNicknameChange: (String) -> Unit,
    onGoogleSignIn: () -> Unit,
    onCreateAccount: () -> Unit,
    warningsFlow: StateFlow<((context: Context) -> String?)?>,
    isSigningIn: Boolean,
    modifier: Modifier = Modifier
) {
    ScreenWithWarnings(
        warningsFlow = warningsFlow,
        modifier = modifier
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

            when (authUiState) {
                AuthUiState.INITIAL -> {
                    InitialAuthContent(
                        onGoogleSignIn = onGoogleSignIn,
                        isSigningIn = isSigningIn
                    )
                }

                AuthUiState.POST_GOOGLE_AUTH -> {
                    PostGoogleAuthContent(
                        draftNickname = draftNickname,
                        validationResult = validationResult,
                        onNicknameChange = onNicknameChange,
                        onCreateAccount = onCreateAccount,
                        isSigningIn = isSigningIn
                    )
                }

                AuthUiState.LOADING -> {
                    LoadingContent()
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun InitialAuthContent(
    onGoogleSignIn: () -> Unit,
    isSigningIn: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Welcome section
        Text(
            text = "Welcome to Madafaker",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Share random thoughts with strangers and discover unexpected perspectives",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        GoogleSignInButton(
            onClick = onGoogleSignIn,
            isLoading = isSigningIn,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun PostGoogleAuthContent(
    draftNickname: String,
    validationResult: MfResult<Unit>?,
    onNicknameChange: (String) -> Unit,
    onCreateAccount: () -> Unit,
    isSigningIn: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Choose your nickname",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        NicknameInputBlock(
            draftNickname = draftNickname,
            validationResult = validationResult,
            onNicknameChange = onNicknameChange
        )

        Spacer(modifier = Modifier.height(24.dp))

        CreateAccountButton(
            onClick = onCreateAccount,
            isLoading = isSigningIn,
            isEnabled = validationResult is MfResult.Success && draftNickname.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun LoadingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Setting up your account...",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
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
fun NicknameInputBlock(
    draftNickname: String,
    validationResult: MfResult<Unit>?,
    onNicknameChange: (String) -> Unit,
) {
    TextField(
        value = draftNickname,
        onValueChange = {
            onNicknameChange(it)
        },
        label = { Text("Enter the desired nickname") },
        placeholder = { Text("Your nickname") },
        singleLine = true,
        trailingIcon = {
            Text(
                text = "${draftNickname.length}/100",
                modifier = Modifier.padding(end = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        textStyle = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
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
}

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = modifier
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        if (isLoading) {
            Text("Signing in...")
        } else {
            Text(
                text = "Log In with Google",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun CreateAccountButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = !isLoading && isEnabled,
        modifier = modifier
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        if (isLoading) {
            Text("Creating account...")
        } else {
            Text(
                text = "Create Account",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
