package com.bbuddies.madafaker.presentation.base

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bbuddies.madafaker.presentation.R
import com.bbuddies.madafaker.presentation.design.components.MovingSunEffect

/**
 * Reusable decorative background component that combines MovingSunEffect and background image.
 * This component extracts the common decorative elements used across different screens.
 */
@Composable
fun DecorativeBackground(
    @DrawableRes backgroundImageRes: Int? = null,
    sunSize: Dp = 300.dp,
    sunAlignment: Alignment = Alignment.TopCenter,
    sunGlowEnabled: Boolean = true,
    sunPadding: Dp = 80.dp,
    sunVisible: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Moving sun effect

        Box(modifier = Modifier.fillMaxSize()) {
            MovingSunEffect(
                size = 64.dp,
                alignment = Alignment.TopStart,
                glowEnabled = true,
                padding = 24.dp
            )

            Image(
                painter = painterResource(id = R.drawable.blur_top_bar),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        // Content on top
        content()
    }
}
