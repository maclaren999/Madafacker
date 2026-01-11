package com.bbuddies.madafaker.presentation.ui.main.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.User
import com.bbuddies.madafaker.presentation.R
import com.bbuddies.madafaker.presentation.base.ScreenWithWarnings
import com.bbuddies.madafaker.presentation.design.components.MadafakerSecondaryButton
import com.bbuddies.madafaker.presentation.design.theme.MadafakerTheme
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


@Composable
fun AccountTab(
    viewModel: AccountTabViewModel,
    onNavigateToAuth: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val showDeleteDialog by viewModel.showDeleteAccountDialog.collectAsState()
    val showLogoutDialog by viewModel.showLogoutDialog.collectAsState()
    val showFeedbackDialog by viewModel.showFeedbackDialog.collectAsState()
    val feedbackText by viewModel.feedbackText.collectAsState()
    val selectedRating by viewModel.selectedRating.collectAsState()
    val isSubmittingFeedback by viewModel.isSubmittingFeedback.collectAsState()

    ScreenWithWarnings(
        warningsFlow = viewModel.warningsFlow
    ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .padding(bottom = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    // Profile Section
                    ProfileSection(
                        user = currentUser,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Account Actions
                AccountActionsSection(
                    onDeleteAccountClick = viewModel::onDeleteAccountClick,
                    onLogoutClick = viewModel::onLogoutClick,
                    onFeedbackClick = viewModel::onFeedbackClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Delete Account Dialog
        if (showDeleteDialog) {
            DeleteAccountDialog(
                onConfirm = {
                    viewModel.sendDeleteAccountEmail(context, currentUser)
                },
                onDismiss = viewModel::dismissDeleteAccountDialog
            )
        }

        // Logout Confirmation Dialog
        if (showLogoutDialog) {
            LogoutConfirmationDialog(
                onConfirm = {
                    viewModel.performLogout(onNavigateToAuth)
                },
                onDismiss = viewModel::dismissLogoutDialog
            )
        }

        // Feedback Dialog
        if (showFeedbackDialog) {
            FeedbackDialog(
                feedbackText = feedbackText,
                selectedRating = selectedRating,
                isSubmitting = isSubmittingFeedback,
                onFeedbackTextChange = viewModel::onFeedbackTextChange,
                onRatingChange = viewModel::onRatingChange,
                onSubmit = viewModel::submitFeedback,
                onDismiss = viewModel::dismissFeedbackDialog
            )
    }
}

@Composable
private fun ProfileSection(
    user: User?,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val userId = user?.id
    val displayId = userId?.let { "${it.take(8)}..." } ?: "--------"
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // User Name - Using H2 style
            Text(
                text = user?.name ?: stringResource(R.string.account_unknown_user),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            // User ID
            Text(
                text = stringResource(R.string.account_user_id_prefix) + displayId,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.clickable(enabled = userId != null) {
                    if (userId != null) {
                        clipboardManager.setText(AnnotatedString(userId))
                    }
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Member since
            Text(
                text = stringResource(R.string.account_member_since_prefix).trimEnd() +
                        " " +
                        formatMemberSince(user?.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
    }
}

private fun formatMemberSince(createdAt: String?): String {
    if (createdAt.isNullOrBlank()) {
        return "Unknown"
    }

    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
    return runCatching {
        Instant.parse(createdAt)
            .atZone(ZoneId.systemDefault())
            .format(formatter)
    }.recoverCatching {
        LocalDateTime.parse(createdAt)
            .atZone(ZoneId.systemDefault())
            .format(formatter)
    }.recoverCatching {
        LocalDate.parse(createdAt).format(formatter)
    }.getOrElse { createdAt }
}

private val previewUser = User(
    id = "preview-user",
    name = "Preview User",
    registrationToken = "preview-token",
    coins = 240,
    createdAt = "2024-01-01",
    updatedAt = "2024-06-01"
)

@Preview(showBackground = true)
@Composable
private fun ProfileSectionPreview() {
    MadafakerTheme(mode = Mode.SHINE) {
        ProfileSection(
            user = previewUser,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountActionsSectionPreview() {
    MadafakerTheme(mode = Mode.SHINE) {
        AccountActionsSection(
            onDeleteAccountClick = {},
            onLogoutClick = {},
            onFeedbackClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DeleteAccountDialogPreview() {
    MadafakerTheme(mode = Mode.SHINE) {
        DeleteAccountDialog(
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LogoutConfirmationDialogPreview() {
    MadafakerTheme(mode = Mode.SHINE) {
        LogoutConfirmationDialog(
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FeedbackDialogPreview() {
    MadafakerTheme(mode = Mode.SHINE) {
        FeedbackDialog(
            feedbackText = "Loving the experience so far!",
            selectedRating = 4,
            isSubmitting = false,
            onFeedbackTextChange = {},
            onRatingChange = {},
            onSubmit = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StarRatingPreview() {
    MadafakerTheme(mode = Mode.SHINE) {
        StarRating(
            rating = 3,
            onRatingChange = {},
            enabled = true
        )
    }
}

@Composable
private fun AccountActionsSection(
    onDeleteAccountClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Logout Button
        MadafakerSecondaryButton(
            text = stringResource(R.string.account_logout_button),
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth()
        )

        // Send Feedback Button
        MadafakerSecondaryButton(
            text = "Send Feedback",
            onClick = onFeedbackClick,
            modifier = Modifier.fillMaxWidth()
        )

        // Delete Account Button
        Button(
            onClick = onDeleteAccountClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Red,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = Color.Red.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, Color.Red),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Account",
                modifier = Modifier.size(20.dp),
                tint = Color.Red
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.account_delete_button),
                style = MaterialTheme.typography.labelLarge,
                color = Color.Red
            )
        }
    }
}

@Composable
private fun DeleteAccountDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Account",
                tint = Color.Red,
                modifier = Modifier.size(24.dp)
            )
        },
        title = {
            Text(
                text = stringResource(R.string.dialog_delete_account_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.dialog_delete_account_message),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.dialog_delete_account_details),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                )
            ) {
                Text(stringResource(R.string.dialog_delete_account_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    )
}

@Composable
private fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Logout",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        },
        title = {
            Text(
                text = stringResource(R.string.dialog_logout_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = stringResource(R.string.dialog_logout_message),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(stringResource(R.string.dialog_logout_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    )
}

@Composable
private fun FeedbackDialog(
    feedbackText: String,
    selectedRating: Int?,
    isSubmitting: Boolean,
    onFeedbackTextChange: (String) -> Unit,
    onRatingChange: (Int?) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = {
            Text(
                text = "Send Feedback",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Star Rating
                Text(
                    text = "Rate your experience (optional):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                StarRating(
                    rating = selectedRating,
                    onRatingChange = onRatingChange,
                    enabled = !isSubmitting
                )

                // Feedback Text Input
                Text(
                    text = "Tell us about your experience (optional):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = onFeedbackTextChange,
                    placeholder = { Text("Tell us about your experience...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    enabled = !isSubmitting,
                    maxLines = 4,
                    supportingText = {
                        Text(
                            text = "${feedbackText.length}/500",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (feedbackText.length > 450) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = !isSubmitting && (selectedRating != null || feedbackText.trim().isNotEmpty()),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isSubmitting) "Submitting..." else "Submit")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun StarRating(
    rating: Int?,
    onRatingChange: (Int?) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..5) {
            IconButton(
                onClick = {
                    if (enabled) {
                        onRatingChange(if (rating == i) null else i)
                    }
                },
                enabled = enabled
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Star $i",
                    tint = if (rating != null && i <= rating) {
                        Color(0xFFFFD700) // Gold color for filled stars
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    },
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        if (rating != null) {
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(
                onClick = { if (enabled) onRatingChange(null) },
                enabled = enabled
            ) {
                Text(
                    text = "Clear",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}



