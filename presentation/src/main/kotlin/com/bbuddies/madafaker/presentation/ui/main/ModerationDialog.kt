package com.bbuddies.madafaker.presentation.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bbuddies.madafaker.common_domain.enums.Mode

//TODO #UI Consider embedding this UI as a state of [SendMessageView] instead of a dialog.
/**
 * Dialog shown when content moderation fails
 */
@Composable
fun ModerationDialog(
    state: ModerationDialogState,
    onDismiss: () -> Unit,
    onSwitchToShadow: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = state.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start
                )

                if (state.showSwitchToShadow) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Shadow mode allows more freedom of expression with minimal content filtering.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start
                    )
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss
                ) {
                    Text("Got it")
                }

                if (state.showSwitchToShadow) {
                    Button(
                        onClick = onSwitchToShadow,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Switch to Shadow")
                    }
                }
            }
        },
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun ModerationDialogPreview() {
    MaterialTheme {
        ModerationDialog(
            state = ModerationDialogState(
                title = "Content Not Allowed",
                message = "Please keep it positive or switch to Shadow mode for uncensored expression!",
                showSwitchToShadow = true,
                currentMode = Mode.SHINE
            ),
            onDismiss = {},
            onSwitchToShadow = {}
        )
    }
}
