package com.bbuddies.madafaker.presentation.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bbuddies.madafaker.presentation.ui.main.MainTab

/**
 * Top Navigation Bar component for unified tab navigation
 * Uses TopLevelDestination pattern for cleaner navigation logic
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationBar(
    navController: NavHostController,
    onTabSelected: (MainTab) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0xFFFFFF),
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                topLevelDestinations.forEach { destination ->
                    val isSelected = currentDestination?.hierarchy?.any {
                        it.route?.contains(destination.route::class.simpleName ?: "") == true
                    } == true

                    Surface(
                        color = Color(0xFFFFFF),
                        onClick = {
                            onTabSelected(destination.tab)
                            navController.navigateToTopLevelDestination(destination)
                        }
                    ) {
                        Text(
                            text = stringResource(destination.tab.titleRes),
                            color = Color(0xFF424242),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun TopNavigationBarPreview() {
    val navController = rememberNavController()
    TopNavigationBar(
        navController = navController,
        onTabSelected = {}
    )
}
