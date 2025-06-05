package com.bbuddies.madafaker.presentation.ui.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.presentation.base.MfResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/* ----------  PALETTE  ---------- */
internal val SunTop = Color(0xFFFFF176)       // bright golden yellow
internal val SunBottom = Color(0xFFF8CE46)    // deeper mustard yellow
internal val SunBody = Color(0xFFFF9800)      // orange rising-sun
internal val CardBg = Color(0xFFFFF9E0)       // off-white for cards
internal val Stripe = Color(0xFFF5C726)       // deeper gold stripe
internal val TextPrimary = Color(0xFF333333)  // charcoal
internal val TextSecondary = Color(0x80333333)// semi-transparent charcoal
internal val HeartRed = Color(0xFFE53935)

/* ----------  MAIN  ---------- */
@Composable
fun MainScreen(
    navController: NavHostController,
    viewModel: MainViewModel
) {
    /* --- TAB SETUP --- */
    val tabs = listOf("write", "feed", "discussions", "account")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    /* --- BACKGROUND GRADIENT + SUN --- */
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(SunTop, SunBottom)
                    )
                )
        ) {
            /* Glowing sun peeking at the very top */
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .align(Alignment.TopCenter)
            ) {
                val radius = size.width * 0.6f
                drawCircle(
                    color = SunBody,
                    radius = radius,
                    center = Offset(x = size.width / 2, y = size.height * 1.2f)
                )
            }

            Column(modifier = Modifier.fillMaxSize()) {

                /* ---------- TAB ROW ---------- */
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    tabs.forEachIndexed { index, label ->
                        val selected = pagerState.currentPage == index
                        val color by animateColorAsState(
                            if (selected) TextPrimary else TextSecondary
                        )
                        Text(
                            text = label,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = color,
                            modifier = Modifier
                                .clickable {
                                    scope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                        )
                    }
                }

                /* ---------- PAGE CONTENT ---------- */
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> WriteTab(viewModel)
                        1 -> FeedTab(viewModel)
                        2 -> DiscussionsTab(viewModel)
                        3 -> AccountTab()
                    }
                }
            }
        }
    }
}

/* ----------  WRITE  ---------- */
@Composable
private fun WriteTab(viewModel: MainViewModel) {
    SendMessageView(viewModel)
}

/* ----------  FEED  ---------- */
@Composable
private fun FeedTab(viewModel: MainViewModel) {
    val incomingMessages by viewModel.incomingMessages.collectAsState()

    when (incomingMessages) {
        is MfResult.Loading -> {
            LoadingView()
        }

        is MfResult.Success -> {
            MessageList((incomingMessages as MfResult.Success<List<Message>>).data.map { it.toFeedMessage() })
        }

        is MfResult.Error -> {
            ErrorView(
                message = (incomingMessages as MfResult.Error<List<Message>>).getErrorString(LocalContext.current),
                onRetry = { viewModel.refreshMessages() }
            )
        }
    }
}

/* ----------  DISCUSSIONS (MY POSTS)  ---------- */
@Composable
private fun DiscussionsTab(viewModel: MainViewModel) {
    val outcomingMessages by viewModel.outcomingMessages.collectAsState()

    when (outcomingMessages) {
        is MfResult.Loading -> {
            LoadingView()
        }

        is MfResult.Success -> {
            MyPostsList((outcomingMessages as MfResult.Success<List<Message>>).data.map { it.toFeedMessage() })
        }

        is MfResult.Error -> {
            ErrorView(
                message = (outcomingMessages as MfResult.Error<List<Message>>).getErrorString(LocalContext.current),
                onRetry = { viewModel.refreshMessages() }
            )
        }
    }
}

/* ----------  UI STATE VIEWS  ---------- */
@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = SunBody,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "loading messages...",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "⚠️",
                fontSize = 48.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = "oops!",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Retry button
            Box(
                modifier = Modifier
                    .clickable { onRetry() }
                    .background(
                        color = SunBody,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "try again",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun MyPostsList(messages: List<FeedMessage>) {
    if (messages.isEmpty()) {
        EmptyStateView(
            emoji = "💬",
            title = "no posts yet",
            subtitle = "your sent messages will appear here once you start sharing"
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        items(messages) { msg ->
            MyPostCard(msg)
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun EmptyStateView(
    emoji: String,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = emoji,
                fontSize = 48.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@Composable
private fun MyPostCard(message: FeedMessage) {
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
                .background(Stripe)
        )

        Column(
            modifier = Modifier
                .background(
                    color = CardBg,
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(16.dp)
                .weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "my message",
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelMedium,
                )

                // Status indicator
                Text(
                    text = "delivered",
                    color = Color(0xFF4CAF50),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Text(
                text = message.body,
                color = TextPrimary,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                maxLines = 6,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Reaction(Icons.Outlined.ThumbUp, message.up, TextSecondary)
                Reaction(Icons.Outlined.KeyboardArrowDown, message.down, TextSecondary)
                Reaction(Icons.Outlined.FavoriteBorder, message.hearts, HeartRed)

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "3 replies",
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

/* ----------  EXTENSION FUNCTIONS  ---------- */
private fun Message.toFeedMessage(): FeedMessage {
    return FeedMessage(
        id = id,
        author = "user_${authorId.take(8)}", // Simplified author display
        body = body,
        up = (0..20).random(), // Mock reaction counts for now
        down = (0..5).random(),
        hearts = (0..15).random()
    )
}

/* ----------  ACCOUNT  ---------- */
@Composable
private fun AccountTab() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Account settings coming soon", color = TextPrimary)
    }
}

/* ----------  MESSAGE LIST & CARD ---------- */
@Composable
private fun MessageList(messages: List<FeedMessage>) {
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
private fun MessageCard(message: FeedMessage) {
    /* left gold stripe + subtle inner shadow via drawWithContent */
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawWithContent {
                drawContent()
                /* inner shadow / edge darken */
                drawRect(
                    color = Color.Black.copy(alpha = 0.04f)
                )
            }
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(Stripe)
        )

        Column(
            modifier = Modifier
                .background(
                    color = CardBg,
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(16.dp)
                .weight(1f)
        ) {
            /* Heading */
            Text(
                text = message.author,
                color = TextSecondary,
                style = MaterialTheme.typography.labelMedium,
            )

            /* Body */
            Text(
                text = message.body,
                color = TextPrimary,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                maxLines = 6,
                overflow = TextOverflow.Ellipsis
            )

            /* Reactions row */
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Reaction(Icons.Outlined.ThumbUp, message.up, TextSecondary)
                Reaction(Icons.Outlined.KeyboardArrowDown, message.down, TextSecondary)
                Reaction(Icons.Outlined.FavoriteBorder, message.hearts, HeartRed)
            }
        }
    }
}

@Composable
private fun Reaction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
        Text(text = count.toString(), color = tint, fontSize = MaterialTheme.typography.labelSmall.fontSize)
    }
}

/* ----------  SAMPLE DATA  ---------- */
private data class FeedMessage(
    val id: String,
    val author: String,
    val body: String,
    val up: Int,
    val down: Int,
    val hearts: Int
)


/* ----------  PREVIEW SETUP  ---------- */
private class PreviewMainViewModel : MainScreenContract {
    private val _draftMessage = MutableStateFlow("Sample preview message")
    override val draftMessage: StateFlow<String> = _draftMessage

    override fun onSendMessage(message: String) {
        // No-op for preview
    }

    override fun onDraftMessageChanged(message: String) {
        _draftMessage.value = message
    }
}

private val previewMainVm = PreviewMainViewModel()

/* ----------  UPDATE COMPOSABLE SIGNATURE  ---------- */
@Composable
fun MainScreen(
    navController: NavHostController,
    viewModel: MainScreenContract // Changed from MainViewModel
) {
    // ... rest of implementation remains the same
}

@Composable
fun SendMessageView(viewModel: MainScreenContract) { // Changed from MainViewModel
    // ... rest of implementation remains the same
}


/* ----------  PREVIEW  ---------- */
@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    /* Fake Nav + VM for preview */
    MainScreen(navController = NavHostController(LocalContext.current), viewModel = previewMainVm)
}
