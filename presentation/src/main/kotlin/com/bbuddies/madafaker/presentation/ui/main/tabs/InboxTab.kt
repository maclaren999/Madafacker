package com.bbuddies.madafaker.presentation.ui.main.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bbuddies.madafaker.common_domain.AppConfig
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.presentation.base.HandleState
import com.bbuddies.madafaker.presentation.ui.main.MainScreenContract
import com.bbuddies.madafaker.presentation.ui.main.MainScreenTheme

@Composable
fun InboxTab(viewModel: MainScreenContract) {
    val incomingMessages by viewModel.incomingMessages.collectAsState()

    incomingMessages.HandleState(
        onRetry = viewModel::refreshMessages
    ) { messages ->
        MessageList(messages.toInboxMessages())
    }
}

@Composable
private fun MessageList(messages: List<InboxMessage>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        items(messages) { msg ->
            MessageCard(msg)
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MessageCard(message: InboxMessage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawWithContent {
                drawContent()
                drawRect(color = Color.Black.copy(alpha = 0.04f))
            }
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(MainScreenTheme.Stripe)
        )

        Column(
            modifier = Modifier
                .background(
                    color = MainScreenTheme.CardBg,
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(16.dp)
                .weight(1f)
        ) {
            Text(
                text = message.author,
                color = MainScreenTheme.TextSecondary,
                style = MaterialTheme.typography.labelMedium,
            )

            Text(
                text = message.body,
                color = MainScreenTheme.TextPrimary,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                maxLines = 6,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                message.up?.let {
                    Reaction(Icons.Outlined.ThumbUp, it, MainScreenTheme.TextSecondary)
                }
                message.down?.let {
                    Reaction(Icons.Outlined.KeyboardArrowDown, it, MainScreenTheme.TextSecondary)
                }
                message.hearts?.let {
                    Reaction(Icons.Outlined.FavoriteBorder, it, MainScreenTheme.HeartRed)
                }
            }
        }
    }
}

@Composable
private fun Reaction(
    icon: ImageVector,
    count: Int,
    tint: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = count.toString(),
            color = tint,
            fontSize = MaterialTheme.typography.labelSmall.fontSize
        )
    }
}

// Extension functions
@Suppress("KotlinConstantConditions")
private fun Message.toInboxMessage(): InboxMessage {
    return InboxMessage(
        id = id,
        author = "user_${authorId.take(8)}", // Simplified author display
        body = body,
        up = if (AppConfig.USE_MOCK_API) (0..20).random() else up,
        down = if (AppConfig.USE_MOCK_API) (0..5).random() else down,
        hearts = if (AppConfig.USE_MOCK_API) (0..15).random() else hearts
    )
}

fun List<Message>.toInboxMessages(): List<InboxMessage> {
    return map { it.toInboxMessage() }
}

// Data class
data class InboxMessage(
    val id: String,
    val author: String,
    val body: String,
    val up: Int?,
    val down: Int?,
    val hearts: Int?
)