package com.bbuddies.madafaker.presentation.ui


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import com.bbuddies.madafaker.presentation.NavigationItem


@Composable
fun SplashScreen(navController: NavHostController) {

    val animationState by remember {
        mutableStateOf(MutableTransitionState(true))
    }
    LaunchedEffect(Unit) {
        animationState.targetState = false
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedVisibility(
            visibleState = animationState,
            exit = fadeOut(animationSpec = tween(durationMillis = 2000))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Hi,Madafaker!", textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineLarge
                )

            }


        }
        if (!animationState.targetState && !animationState.currentState) {
            //navigate to another route in NavHost
            navController.navigate(NavigationItem.Account.route)

        }
    }
}



