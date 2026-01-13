package com.bbuddies.madafaker.presentation.ui.auth

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.presentation.R
import com.bbuddies.madafaker.presentation.base.ScreenWithWarnings
import com.bbuddies.madafaker.presentation.design.components.MadafakerSecondaryButton
import com.bbuddies.madafaker.presentation.design.components.MadafakerTextField
import com.bbuddies.madafaker.presentation.design.components.MovingSunEffect
import com.bbuddies.madafaker.presentation.design.theme.MadafakerTheme
import com.bbuddies.madafaker.presentation.design.theme.ShadowSunGradient
import com.bbuddies.madafaker.presentation.design.theme.ShineSunGradient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun AuthScreen(
    navAction: AuthNavigationAction,
    viewModel: AuthViewModel,
    redirectRoute: String? = null,
    modifier: Modifier = Modifier
) {
    val authUiState by viewModel.authUiState.collectAsState()
    val draftNickname by viewModel.draftNickname.collectAsState()
    val validationResult by viewModel.nicknameDraftValidationResult.collectAsState()
    val isSigningIn by viewModel.isSigningIn.collectAsState()
    val currentMode by viewModel.currentMode.collectAsState()
    val context = LocalContext.current

    BackHandler(enabled = authUiState == AuthUiState.POST_GOOGLE_AUTH) {
        viewModel.onPostGoogleAuthBack()
    }

    AuthScreen(
        authUiState = authUiState,
        draftNickname = draftNickname,
        validationResult = validationResult,
        onNicknameChange = { newDraft ->
            viewModel.onDraftNickChanged(newDraft)
        },
        onGoogleSignIn = {
            viewModel.onGoogleSignIn(
                context = context,
                onSuccessfulSignIn = { notificationPermissionHelper ->
                    navAction.navigateAfterSuccessfulAuth(
                        notificationPermissionHelper = notificationPermissionHelper,
                        redirectRoute = redirectRoute
                    )
                }
            )
        },
        onCreateAccount = {
            viewModel.onCreateAccount(
                onSuccessfulCreation = { notificationPermissionHelper ->
                    navAction.navigateAfterSuccessfulAuth(
                        notificationPermissionHelper = notificationPermissionHelper,
                        redirectRoute = redirectRoute
                    )
                }
            )
        },
        warningsFlow = viewModel.warningsFlow,
        isSigningIn = isSigningIn,
        currentMode = currentMode,
        modifier = modifier
    )
}

@Composable
fun AuthScreen(
    authUiState: AuthUiState,
    draftNickname: String,
    validationResult: ValidationState?,
    onNicknameChange: (String) -> Unit,
    onGoogleSignIn: () -> Unit,
    onCreateAccount: () -> Unit,
    warningsFlow: StateFlow<((context: Context) -> String?)?>,
    isSigningIn: Boolean,
    currentMode: Mode,
    modifier: Modifier = Modifier
) {
    val sunColors = if (currentMode == Mode.SHINE) ShineSunGradient else ShadowSunGradient
    val blurResId = if (currentMode == Mode.SHINE) R.drawable.blur else R.drawable.blur_dark

    ScreenWithWarnings(
        warningsFlow = warningsFlow,
        modifier = modifier
    ) {
        MovingSunEffect(
            baseColors = sunColors,
            size = 300.dp,
            alignment = Alignment.TopCenter,
            glowEnabled = true,
            padding = 80.dp
        )

        Image(
            painter = painterResource(id = blurResId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(modifier = Modifier.weight(1f))
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
        // Welcome section - Using H1 style
        Text(
            text = stringResource(R.string.auth_welcome_title),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = stringResource(R.string.auth_welcome_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        MadafakerSecondaryButton(
            text = if (isSigningIn)
                stringResource(R.string.auth_signing_in)
            else
                stringResource(R.string.auth_login_with_google),
            onClick = onGoogleSignIn,
            enabled = !isSigningIn,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun PostGoogleAuthContent(
    draftNickname: String,
    validationResult: ValidationState?,
    onNicknameChange: (String) -> Unit,
    onCreateAccount: () -> Unit,
    isSigningIn: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.auth_choose_nickname_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        NicknameInputBlock(
            draftNickname = draftNickname,
            validationResult = validationResult,
            onNicknameChange = onNicknameChange
        )

        Spacer(modifier = Modifier.height(24.dp))

        MadafakerSecondaryButton(
            text = if (isSigningIn)
                stringResource(R.string.auth_creating_account)
            else
                stringResource(R.string.auth_create_account),
            onClick = onCreateAccount,
            enabled = !isSigningIn && validationResult is ValidationState.Success && draftNickname.isNotBlank(),
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
            text = stringResource(R.string.auth_setting_up_account),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun NicknameInputBlock(
    draftNickname: String,
    validationResult: ValidationState?,
    onNicknameChange: (String) -> Unit,
) {
    MadafakerTextField(
        value = draftNickname,
        onValueChange = onNicknameChange,
        placeholder = stringResource(R.string.auth_nickname_input_placeholder),
        modifier = Modifier.fillMaxWidth(),
        supportingText = {
            Column {
                // Character count
                Text(
                    text = "${draftNickname.length}/${NicknameDraftValidator.MAX_NICKNAME_LENGTH}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Validation result
                when (validationResult) {
                    is ValidationState.Loading -> Text(
                        text = stringResource(R.string.auth_validating_nickname),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    is ValidationState.Error -> Text(
                        text = validationResult.getErrorString.invoke(LocalContext.current),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Red
                    )

                    else -> {}
                }
            }

        }
    )
}


@Preview
@Composable
fun AuthScreenPreview() {
    MadafakerTheme(Mode.SHINE) {
        AuthScreen(
            authUiState = AuthUiState.POST_GOOGLE_AUTH,
            draftNickname = "Nickname",
            validationResult = ValidationState.Success,
            onNicknameChange = {},
            onGoogleSignIn = {},
            onCreateAccount = {},
            warningsFlow = MutableStateFlow(null),
            isSigningIn = false,
            currentMode = Mode.SHINE
        )
    }
}

@Preview
@Composable
fun AuthScreenShadowPreview() {
    MadafakerTheme(Mode.SHADOW) {
        AuthScreen(
            authUiState = AuthUiState.POST_GOOGLE_AUTH,
            draftNickname = "Nickname",
            validationResult = ValidationState.Success,
            onNicknameChange = {},
            onGoogleSignIn = {},
            onCreateAccount = {},
            warningsFlow = MutableStateFlow(null),
            isSigningIn = false,
            currentMode = Mode.SHADOW
        )
    }
}
