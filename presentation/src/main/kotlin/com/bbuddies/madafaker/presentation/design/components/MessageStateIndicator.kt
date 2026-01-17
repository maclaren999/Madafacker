package com.bbuddies.madafaker.presentation.design.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bbuddies.madafaker.common_domain.model.MessageState
import com.bbuddies.madafaker.presentation.R


@Composable
fun MessageStateIndicator(messageState: MessageState) {
    val (icon, tint, contentDescription) = when (messageState) {
        MessageState.PENDING -> Triple(
            Icons.Outlined.Refresh,
            Color(0xFFFF9800),
            stringResource(R.string.message_sending)
        )

        MessageState.SENT -> Triple(
            Icons.Outlined.Done,
            Color(0xFF4CAF50),
            stringResource(R.string.message_delivered)
        )
        MessageState.FAILED -> Triple(Icons.Outlined.Close, Color(0xFFE53935), stringResource(R.string.message_failed))
    }

    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = tint,
        modifier = Modifier.size(18.dp)
    )
}