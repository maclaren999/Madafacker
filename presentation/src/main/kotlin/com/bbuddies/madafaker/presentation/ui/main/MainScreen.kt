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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
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
                        1 -> FeedTab()
                        2 -> DiscussionsTab()
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
private fun FeedTab() {
    MessageList(dummyMessages)
}

/* ----------  DISCUSSIONS  ---------- */
@Composable
private fun DiscussionsTab() {
    MessageList(dummyMessages.take(2)) // placeholder
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

private val dummyMessages = listOf(
    FeedMessage(
        id = "1",
        author = "sunny_dev",
        body = "Every day is a new chance to shine ✨",
        up = 14,
        down = 1,
        hearts = 20
    ),
    FeedMessage(
        id = "2",
        author = "optimist98",
        body = "Sending good vibes to whoever needs them right now!",
        up = 8,
        down = 0,
        hearts = 11
    ),
    FeedMessage(
        id = "3",
        author = "random_thoughts",
        body = "Why do we park on driveways and drive on parkways?",
        up = 5,
        down = 3,
        hearts = 4
    )
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
