package com.bbuddies.madafaker.presentation.design.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bbuddies.madafaker.presentation.design.theme.ShadowSunGradient
import com.bbuddies.madafaker.presentation.design.theme.ShineSunGradient
import kotlinx.coroutines.delay


@Composable
fun MovingSunEffect(
    visible: Boolean = true,
    baseColors: List<Color> = ShineSunGradient,
    size: Dp = 300.dp,
    alignment: Alignment = Alignment.TopCenter,
    enableInteraction: Boolean = true,
    saturationMultiplier: Float = 0.4f,
    animationDuration: Int = 600,
    glowEnabled: Boolean = false,
    padding: Dp = 0.dp,
    onAnimationComplete: () -> Unit = {}
) {
    var targetCenter by remember { mutableStateOf<Offset?>(null) }
    val animatableCenter = remember { Animatable(Offset(0f, 0f), Offset.VectorConverter) }

    LaunchedEffect(targetCenter) {
        targetCenter?.let { center ->
            animatableCenter.animateTo(
                targetValue = center,
                animationSpec = tween(durationMillis = animationDuration, easing = FastOutSlowInEasing)
            )
        }
    }

    var isPressed by remember { mutableStateOf(false) }
    val saturation by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = tween(400)
    )

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(1000)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(1000)),
        label = "sun-animation"
    ) {
        Box(
            contentAlignment = alignment,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Canvas(
                modifier = Modifier
                    .size(if (glowEnabled) size * 1.3f else size)
                    .then(
                        if (enableInteraction) {
                            Modifier.pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = { offset ->
                                        targetCenter = offset
                                        isPressed = true
                                        tryAwaitRelease()
                                        isPressed = false
                                    }
                                )
                            }
                        } else Modifier
                    )
            ) {
                val canvasSize = this.size
                val centerDefault = Offset(canvasSize.width / 2, canvasSize.height / 2)
                val center = if (animatableCenter.value != Offset(0f, 0f)) {
                    animatableCenter.value
                } else centerDefault
                val radius = (size.toPx() / 2)

                fun saturateColor(color: Color, factor: Float): Color {
                    val hsv = FloatArray(3)
                    android.graphics.Color.colorToHSV(color.toArgb(), hsv)
                    hsv[1] = (hsv[1] + factor).coerceAtMost(1f)
                    return Color(android.graphics.Color.HSVToColor(hsv))
                }

                val saturatedColors = baseColors.map { color ->
                    saturateColor(color, saturation * saturationMultiplier)
                }

                val lightestSunColor = saturatedColors.first()
                val outerGradient = Brush.radialGradient(
                    colors = listOf(
                        lightestSunColor.copy(alpha = 1f),
                        lightestSunColor.copy(alpha = 0.9f),
                        lightestSunColor.copy(alpha = 0.8f),
                        lightestSunColor.copy(alpha = 0.7f),
                        lightestSunColor.copy(alpha = 0.5f),
                        Color.Transparent
                    ),
                    center = centerDefault,
                    radius = radius * 1.4f
                )

                val gradient = Brush.radialGradient(
                    colors = saturatedColors,
                    center = center,
                    radius = radius
                )

                drawCircle(
                    brush = outerGradient,
                    center = centerDefault,
                    radius = radius * 1.8f
                )

                drawCircle(
                    brush = gradient,
                    center = centerDefault,
                    radius = radius
                )
            }
        }

        LaunchedEffect(visible) {
            if (!visible) {
                delay(1000)
                onAnimationComplete()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MovingSunEffectPreview() {
    // Маленькое солнце в углу
    MovingSunEffect(
        size = 100.dp,
        alignment = Alignment.TopEnd,
        enableInteraction = false,
        glowEnabled = true
    )

// Луна для темной темы
    MovingSunEffect(
        baseColors = ShadowSunGradient,
        size = 200.dp,
        alignment = Alignment.Center,
        glowEnabled = true,
    )

// Декоративный элемент без взаимодействия
    MovingSunEffect(
        size = 50.dp,
        alignment = Alignment.BottomStart,
        enableInteraction = false,
        animationDuration = 300
    )
}
