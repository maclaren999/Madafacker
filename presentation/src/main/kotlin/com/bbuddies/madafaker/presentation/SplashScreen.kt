package com.bbuddies.madafaker.presentation


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController


@Composable
fun SplashScreen(navController: NavHostController) {

    val animationState by remember {
        mutableStateOf(MutableTransitionState(false))
    }
    LaunchedEffect(Unit) {
        animationState.targetState = true
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedVisibility(
            visible = false,
            exit = scaleOut(animationSpec = tween(durationMillis = 200))
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
            ) {}

        }
        if (!animationState.targetState && !animationState.currentState) {
            //navigate to another route in NavHost
            navController.navigate(NavigationItem.Main.route)

        }
    }
}



