package com.bbuddies.madafaker.presentation.ui.permission

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bbuddies.madafaker.presentation.R
import com.bbuddies.madafaker.presentation.ui.permission.NotificationPermissionNavigationAction
import com.bbuddies.madafaker.presentation.design.components.MovingSunEffect
import com.bbuddies.madafaker.presentation.design.components.MadafakerSecondaryButton
import com.bbuddies.madafaker.presentation.design.components.MadafakerTextButton

@Composable
fun NotificationPermissionScreen(
    navAction: NotificationPermissionNavigationAction,
    viewModel: NotificationPermissionViewModel,
    modifier: Modifier = Modifier
) {
    val permissionState by viewModel.permissionState.collectAsState()
    val showSettingsPrompt by viewModel.showSettingsPrompt.collectAsState()
    val shouldNavigateToMain by viewModel.shouldNavigateToMain.collectAsState()

    // Handle automatic navigation when permission is already granted or after successful grant
    LaunchedEffect(shouldNavigateToMain) {
        if (shouldNavigateToMain) {
            navAction.navigateToMainAfterPermission()
            viewModel.onNavigationHandled()
        }
    }

    NotificationPermissionScreen(
        permissionState = permissionState,
        showSettingsPrompt = showSettingsPrompt,
        onRequestPermission = viewModel::requestPermission,
        onSkip = viewModel::onSkip,
        onPermissionGranted = viewModel::onPermissionGranted,
        onPermissionDenied = viewModel::onPermissionDenied,
        onOpenSettings = viewModel::openSettings,
        onDismissSettingsPrompt = viewModel::dismissSettingsPrompt,
        modifier = modifier
    )
}

@Composable
fun NotificationPermissionScreen(
    permissionState: NotificationPermissionState,
    showSettingsPrompt: Boolean,
    onRequestPermission: () -> Unit,
    onSkip: () -> Unit,
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismissSettingsPrompt: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle Android 13+ notification permission
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            onPermissionDenied()
        }
    }

    // Request permission when needed
    LaunchedEffect(permissionState) {
        when (permissionState) {
            is NotificationPermissionState.ShouldRequest -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    // For older versions, notifications are enabled by default
                    onPermissionGranted()
                }
            }

            is NotificationPermissionState.AlreadyGranted -> {
                // Permission already granted, navigation will be handled by LaunchedEffect above
            }

            else -> {}
        }
    }

    // Don't show the UI if permission is already granted
    if (permissionState is NotificationPermissionState.AlreadyGranted) {
        return
    }

    MovingSunEffect(
        size = 300.dp,
        alignment = Alignment.TopCenter,
        glowEnabled = true,
        padding = 80.dp
    )

    Image(painter = painterResource(id = R.drawable.blur),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop

    )

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Notification Icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title - Using H2 style
            Text(
                text = stringResource(R.string.notification_permission_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Text(
                text = stringResource(R.string.notification_permission_description),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Enable Notifications Button
            MadafakerSecondaryButton(
                text = stringResource(R.string.notification_permission_enable),
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Skip Button
            MadafakerTextButton(
                text = stringResource(R.string.notification_permission_skip),
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Settings prompt snackbar
        if (showSettingsPrompt) {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Snackbar(
                    action = {
                        TextButton(
                            onClick = {
                                onOpenSettings()
                                onDismissSettingsPrompt()
                            }
                        ) {
                            Text(stringResource(R.string.notification_permission_settings), color = Color.White)
                        }
                    },
                    dismissAction = {
                        TextButton(onClick = onDismissSettingsPrompt) {
                            Text(stringResource(R.string.notification_permission_dismiss), color = Color.White)
                        }
                    }
                ) {
                    Text(stringResource(R.string.notification_permission_snackbar))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationPermissionScreenPreview() {
    NotificationPermissionScreen(
        permissionState = NotificationPermissionState.Initial,
        showSettingsPrompt = false,
        onRequestPermission = {},
        onSkip = {},
        onPermissionGranted = {},
        onPermissionDenied = {},
        onOpenSettings = {},
        onDismissSettingsPrompt = {}
    )
}