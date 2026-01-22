package com.bbuddies.madafaker.presentation.design.components

import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupPositionProvider
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.presentation.R
import com.bbuddies.madafaker.presentation.design.theme.ShadowBackgroundGradient
import com.bbuddies.madafaker.presentation.design.theme.ShadowSunGradient
import com.bbuddies.madafaker.presentation.design.theme.ShineBackgroundGradient
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeBackground(
    mode: Mode,
    showDecorative: Boolean = false,
    showModeTip: Boolean = false,
    onModeToggle: () -> Unit = {},
    onTooltipDismissed: () -> Unit = {},
    tooltipAlignment: Alignment = Alignment.TopStart,
    tooltipPadding: PaddingValues = PaddingValues(16.dp),
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

    val tooltipState = rememberTooltipState(isPersistent = false)
    val tooltipPositionProvider = rememberAboveTooltipPositionProvider()

    LaunchedEffect(showDecorative, showModeTip) {
        if (showDecorative && showModeTip) {
            tooltipState.show()
            // Auto-dismiss tooltip after 5 seconds
            kotlinx.coroutines.delay(5000)
            if (tooltipState.isVisible) {
                tooltipState.dismiss()
                onTooltipDismissed()
            }
        } else {
            tooltipState.dismiss()
        }
    }

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
                        size = 92.dp,
                        alignment = Alignment.TopStart,
                        glowEnabled = true,
                        padding = 16.dp,
                        onTap = onModeToggle
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

        if (showDecorative) {
            TooltipBox(
                positionProvider = tooltipPositionProvider,
                state = tooltipState,
                tooltip = {
                    PlainTooltip(
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp),
                        contentColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                        containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = "Tap the sun to switch modes",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },

                ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .height(60.dp)
                        .width(120.dp)
                        .alpha(0f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            Log.i("ModeBackground", "Decorative sun tapped")
                            if (tooltipState.isVisible) {
                                tooltipState.dismiss()
                                onTooltipDismissed()
                            }
                            onModeToggle()
                        }
                )
            }
        }
    }
}

@Composable
private fun rememberAboveTooltipPositionProvider(
    spacingBetweenTooltipAndAnchor: Dp = 4.dp
): PopupPositionProvider {
    val spacingPx = with(LocalDensity.current) { spacingBetweenTooltipAndAnchor.roundToPx() }

    return remember(spacingPx) {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset {
                val centeredX =
                    anchorBounds.left + (anchorBounds.width - popupContentSize.width) / 2
                val clampedX = centeredX.coerceIn(
                    0,
                    (windowSize.width - popupContentSize.width).coerceAtLeast(0)
                )

                val y = (anchorBounds.top - popupContentSize.height - spacingPx).coerceAtLeast(0)

                return IntOffset(clampedX, y)
            }
        }
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
    ModeBackground(mode = mode, true, showModeTip = true) {
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
                    Mode.SHINE -> Color.Black
                    Mode.SHADOW -> Color.White
                }
            )
            Text(
                text = "Preview content",
                fontSize = 16.sp,
                color = when (mode) {
                    Mode.SHINE -> Color.DarkGray
                    Mode.SHADOW -> Color.LightGray
                }
            )
        }
    }
}
