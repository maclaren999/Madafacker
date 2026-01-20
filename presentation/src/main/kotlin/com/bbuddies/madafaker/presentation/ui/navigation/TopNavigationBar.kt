package com.bbuddies.madafaker.presentation.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.presentation.R
import com.bbuddies.madafaker.presentation.design.theme.MadafakerTheme
import com.bbuddies.madafaker.presentation.ui.main.MainTab

/**
 * Top Navigation Bar component for unified tab navigation
 * Uses TopLevelDestination pattern for cleaner navigation logic
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationBar(
    navController: NavHostController,
    mode: Mode,
    onTabSelected: (MainTab) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val navigateToDestination: (TopLevelDestination) -> Unit = { destination ->
        onTabSelected(destination.tab)
        navController.navigateToTopLevelDestination(destination)
    }
    val underlineRes =
        if (mode == Mode.SHADOW) R.drawable.underline_dark else R.drawable.underline_light

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp)
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

                    val textStyle = if (isSelected) {
                        MaterialTheme.typography.headlineMedium
                    } else {
                        MaterialTheme.typography.bodyLarge
                    }

                    Surface(
                        color = Color(0xFFFFFF),
                        onClick = {
                            navigateToDestination(destination)
                        }
                    ) {
                        Column {
                            Text(
                                text = stringResource(destination.tab.titleRes),
                                color = Color(0xFF424242),
                                style = textStyle
                            )
                            if(isSelected) {
                                Image(
                                    painter = painterResource(underlineRes),
                                    contentDescription = null,
                                )
                            }

                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun TopNavigationBarPreview() {
    MadafakerTheme(Mode.SHINE) {
        val navController = rememberNavController()
        TopNavigationBar(
            navController = navController,
            mode = Mode.SHINE,
            onTabSelected = {}
        )
    }
}
