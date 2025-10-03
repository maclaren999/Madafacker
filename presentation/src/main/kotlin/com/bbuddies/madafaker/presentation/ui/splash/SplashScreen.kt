package com.bbuddies.madafaker.presentation.ui.splash


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.bbuddies.madafaker.presentation.R
import kotlinx.coroutines.delay


@Composable
fun SplashScreen(
    navAction: SplashNavigationAction,
    splashViewModel: SplashViewModel,
    modifier: Modifier = Modifier
) {
    val navigationEvent by splashViewModel.navigationEvent.collectAsState()
    val userName = "Madafaker"
    val animationState = splashViewModel.animationState

    LaunchedEffect(navigationEvent) {
        animationState.targetState = false
        delay(500)
        navigationEvent?.let { destination ->
            navAction.navigateBasedOnDestination(destination)
        }
    }

    AnimatedVisibility(
        visibleState = animationState,
        exit = fadeOut(animationSpec = tween(durationMillis = 2000)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
                Text(
                    text = stringResource(R.string.splash_greeting, userName), textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineLarge
                )
            }
        }
}


