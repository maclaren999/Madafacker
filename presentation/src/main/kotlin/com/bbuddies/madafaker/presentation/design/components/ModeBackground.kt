package com.bbuddies.madafaker.presentation.design.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.presentation.R
import com.bbuddies.madafaker.presentation.design.theme.ShadowBackgroundGradient
import com.bbuddies.madafaker.presentation.design.theme.ShineBackgroundGradient

/**
 * Layered background component that changes based on the current mode.
 *
 * Layers (bottom to top):
 * 1. Mode-specific gradient (ShineBackgroundGradient or ShadowBackgroundGradient)
 * 2. Handwriting PNG overlay (base_screen_handwriting_bg.png)
 *    - 5% opacity for SHINE mode
 *    - 2.5% opacity for SHADOW mode (50% of the PNG's built-in 5% opacity)
 * 3. Repeating pattern (sun_mode_bg_pattern_base.png)
 */
@Composable
fun ModeBackground(
    mode: Mode,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = when (mode) {
                    Mode.SHINE -> ShineBackgroundGradient
                    Mode.SHADOW -> ShadowBackgroundGradient
                }
            )
    ) {
        // Layer 2: Handwriting PNG overlay
        Image(
            painter = painterResource(id = R.drawable.base_screen_handwriting_bg),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(
                    when (mode) {
                        Mode.SHINE -> 1.0f // PNG already has 5% opacity built-in
                        Mode.SHADOW -> 0.5f // 50% of PNG's 5% = 2.5% total opacity
                    }
                ),
            contentScale = ContentScale.Crop
        )

        // Layer 3: Repeating pattern
        RepeatingPatternLayer(
            mode = mode,
            modifier = Modifier.fillMaxSize()
        )

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


