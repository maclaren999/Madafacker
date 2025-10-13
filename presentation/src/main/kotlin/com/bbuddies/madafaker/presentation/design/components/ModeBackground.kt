package com.bbuddies.madafaker.presentation.design.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.presentation.R
import com.bbuddies.madafaker.presentation.design.theme.ShadowBackgroundGradient
import com.bbuddies.madafaker.presentation.design.theme.ShineBackgroundGradient
import androidx.compose.ui.Alignment
import com.bbuddies.madafaker.presentation.design.theme.ShadowSunGradient
import com.bbuddies.madafaker.presentation.design.theme.ShineSunGradient

/**
 * Layered background component that changes based on the current mode.
 *
 * Layers (bottom to top):
 * 1. Mode-specific gradient (ShineBackgroundGradient or ShadowBackgroundGradient)
 * 2. Handwriting PNG overlay (base_screen_handwriting_bg.png)
 *    - 5% opacity for SHINE mode
 *    - 2.5% opacity for SHADOW mode (50% of the PNG's built-in 5% opacity)
 * 3. Repeating pattern (sun_mode_bg_pattern_base.png)
 * 4. Decorative elements (MovingSunEffect + blur overlay) - optional
 *
 * Animations:
 * - Crossfade transition between mode gradients (500ms)
 * - Animated alpha for handwriting overlay (400ms)
 * - Animated decorative elements (500ms)
 */
@Composable
fun ModeBackground(
    mode: Mode,
    showDecorative: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Animated alpha for handwriting overlay
    val handwritingAlpha by animateFloatAsState(
        targetValue = when (mode) {
            Mode.SHINE -> 1.0f // PNG already has 5% opacity built-in
            Mode.SHADOW -> 0.5f // 50% of PNG's 5% = 2.5% total opacity
        },
        animationSpec = tween(durationMillis = 400),
        label = "handwriting_alpha"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Layer 1: Animated gradient background with crossfade
        Crossfade(
            targetState = mode,
            animationSpec = tween(durationMillis = 500),
            label = "background_gradient",
            modifier = Modifier.fillMaxSize()
        ) { currentMode ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = when (currentMode) {
                            Mode.SHINE -> ShineBackgroundGradient
                            Mode.SHADOW -> ShadowBackgroundGradient
                        }
                    )
            )
        }

        // Layer 2: Handwriting PNG overlay with animated alpha
        Image(
            painter = painterResource(id = R.drawable.base_screen_handwriting_bg),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(handwritingAlpha),
            contentScale = ContentScale.Crop
        )

        // Layer 3: Repeating pattern
        RepeatingPatternLayer(
            mode = mode,
            modifier = Modifier.fillMaxSize()
        )

        // Layer 4: Decorative elements with crossfade animation
        if (showDecorative) {
            Crossfade(
                targetState = mode,
                animationSpec = tween(durationMillis = 500),
                label = "decorative_elements",
                modifier = Modifier.fillMaxSize()
            ) { currentMode ->
                Box(modifier = Modifier.fillMaxSize()) {
                    MovingSunEffect(
                        baseColors = if (currentMode == Mode.SHINE) ShineSunGradient else ShadowSunGradient,
                        size = 72.dp,
                        alignment = Alignment.TopStart,
                        glowEnabled = true,
                        padding = 24.dp
                    )

                    Image(
                        painter = painterResource(
                            id = if (currentMode == Mode.SHINE)
                                R.drawable.blur_top_bar_light
                            else
                                R.drawable.blur_top_bar_dark
                        ),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // Content on top of all background layers
        content()
    }
}

/**
 * Creates a repeating pattern layer using Canvas to tile the pattern image.
 */
@Composable
private fun RepeatingPatternLayer(
    mode: Mode,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val patternBitmap = remember {
        ImageBitmap.imageResource(context.resources, R.drawable.sun_mode_bg_pattern_base)
    }

    Canvas(modifier = modifier) {
        drawRepeatingPattern(
            bitmap = patternBitmap,
            mode = mode
        )
    }
}

/**
 * Draws the repeating pattern across the entire canvas.
 */
private fun DrawScope.drawRepeatingPattern(
    bitmap: ImageBitmap,
    mode: Mode
) {
    val patternWidth = bitmap.width.toFloat()
    val patternHeight = bitmap.height.toFloat()

    // Calculate how many tiles we need to cover the canvas
    val tilesX = (size.width / patternWidth).toInt() + 1
    val tilesY = (size.height / patternHeight).toInt() + 1

    // Draw the pattern tiles
    for (x in 0..tilesX) {
        for (y in 0..tilesY) {
            val offsetX = x * patternWidth
            val offsetY = y * patternHeight

            // Only draw if the tile is within or overlapping the canvas
            if (offsetX < size.width && offsetY < size.height) {
                drawImage(
                    image = bitmap,
                    topLeft = androidx.compose.ui.geometry.Offset(offsetX, offsetY),
                    alpha = 0.1f // Very subtle pattern overlay, no color filter
                )
            }
        }
    }
}


@Preview(name = "Shine Mode", showBackground = true)
@Composable
private fun ModeBackgroundShinePreview() {
    ModeBackgroundPreview(mode = Mode.SHINE)
}

@Preview(name = "Shadow Mode", showBackground = true)
@Composable
private fun ModeBackgroundShadowPreview() {
    ModeBackgroundPreview(mode = Mode.SHADOW)
}

@Composable
private fun ModeBackgroundPreview(mode: Mode) {
    ModeBackground(mode = mode) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "${mode.name} Mode",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = when (mode) {
                    Mode.SHINE -> androidx.compose.ui.graphics.Color.Black
                    Mode.SHADOW -> androidx.compose.ui.graphics.Color.White
                }
            )
            Text(
                text = "Preview content",
                fontSize = 16.sp,
                color = when (mode) {
                    Mode.SHINE -> androidx.compose.ui.graphics.Color.DarkGray
                    Mode.SHADOW -> androidx.compose.ui.graphics.Color.LightGray
                }
            )
        }
    }
}
