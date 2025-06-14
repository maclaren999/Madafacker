package com.bbuddies.madafaker.presentation.ui.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bbuddies.madafaker.presentation.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

enum class MainTab(val titleRes: Int) {
    WRITE(R.string.tab_write),
    INBOX(R.string.tab_inbox),
    MY_POSTS(R.string.tab_my_posts),
    ACCOUNT(R.string.tab_account)
}

@Composable
fun MainScreenTabs(
    pagerState: PagerState,
    scope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    val tabs = MainTab.entries.toTypedArray()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        tabs.forEachIndexed { index, tab ->
            TabItem(
                tab = tab,
                isSelected = pagerState.currentPage == index,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            )
        }
    }
}

@Composable
private fun TabItem(
    tab: MainTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color by animateColorAsState(
        if (isSelected) MainScreenTheme.TextPrimary else MainScreenTheme.TextSecondary,
        label = "tab_color"
    )

    Text(
        text = stringResource(tab.titleRes),
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        color = color,
        modifier = modifier.clickable { onClick() }
    )
}