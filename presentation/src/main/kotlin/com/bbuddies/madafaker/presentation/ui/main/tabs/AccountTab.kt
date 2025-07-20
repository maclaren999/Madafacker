package com.bbuddies.madafaker.presentation.ui.main.tabs

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import com.bbuddies.madafaker.common_domain.model.User
import com.bbuddies.madafaker.presentation.base.ScreenWithWarnings
import com.bbuddies.madafaker.presentation.ui.main.MainScreenTheme

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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Profile Section
            ProfileSection(
                user = currentUser,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Account Actions
            AccountActionsSection(
                onDeleteAccountClick = viewModel::onDeleteAccountClick,
                onLogoutClick = viewModel::onLogoutClick,
                onFeedbackClick = viewModel::onFeedbackClick,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))
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
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MainScreenTheme.CardBg
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MainScreenTheme.TextSecondary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile Avatar",
                    modifier = Modifier.size(64.dp),
                    tint = MainScreenTheme.TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Name
            Text(
                text = user?.name ?: "Unknown User",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MainScreenTheme.TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // User ID
            Text(
                text = "ID: ${user?.id?.take(8) ?: "--------"}...",
                style = MaterialTheme.typography.bodyMedium,
                color = MainScreenTheme.TextSecondary
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Member since
            Text(
                text = "Member since ${user?.createdAt ?: "Unknown"}",
                style = MaterialTheme.typography.bodySmall,
                color = MainScreenTheme.TextSecondary.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Coins
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "💰",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${user?.coins ?: 0} coins",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MainScreenTheme.TextPrimary
                )
            }
        }
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
        Button(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Logout",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Log Out",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }

        // Send Feedback Button
        Button(
            onClick = onFeedbackClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Send Feedback",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Send Feedback",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }

        // Delete Account Button
        OutlinedButton(
            onClick = onDeleteAccountClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.Red
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Account",
                modifier = Modifier.size(20.dp),
                tint = Color.Red
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Delete Account",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
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
                text = "Delete Account",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "To delete your account, you will be redirected to send us an email request.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "We will process your request and permanently delete all your data within 7 business days.",
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
                Text("Send Email Request")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
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
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Logout",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        },
        title = {
            Text(
                text = "Log Out",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Are you sure you want to log out? All your local data will be cleared and you'll need to create a new account to use the app again.",
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
                Text("Log Out")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
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