package com.bbuddies.madafaker.presentation.design.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Design System Colors
val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)
val LightGray = Color(0xFF9E9E9E)
val DarkGray = Color(0xFF6C6C6C)

// Button Colors
val ButtonOrangeStart = Color(0xFFFBB120)
val ButtonOrangeMiddle = Color(0xFFFF9548)
val ButtonOrangeEnd = Color(0xFFFF6B00)


// Text Colors
val TextPrimary = Color(0xFF131313)
val HeadingPrimary = Color(0xFFB81A17)

// Background Gradients for SHINE and SHADOW modes
// SHINE mode gradient (BG sun)
val ShineGradientColors = listOf(
    Color(0xFFFBF1AD), // #FBF1AD
    Color(0xFFFBEC65), // #FBEC65
    Color(0xFFFB6000)  // #FB6000
)
// SHADOW mode gradient (BG moon)
val ShadowGradientColors = listOf(
    Color(0xFF111111), // #111111
    Color(0xFF000000)  // #000000
)

val ShineSunGradient = listOf(
    Color(0xFFFFF7EB),
    Color(0xFFFFD54F),
    Color(0xFFFFA726)
)

val ShadowSunGradient = listOf(
    Color(0xFFAC1212), // Dark red middle
    Color(0xFF8B0000), // Dark red middle
    Color(0xFF1A0033)  // Very dark purple outer
)

// SHINE mode background gradient
val ShineBackgroundGradient = Brush.verticalGradient(
    colors = ShineGradientColors
)

// SHADOW mode background gradient
val ShadowBackgroundGradient = Brush.verticalGradient(
    colors = ShadowGradientColors
)


// Button Gradient
val ButtonGradient = Brush.radialGradient(
    colors = listOf(
        ButtonOrangeStart,
        ButtonOrangeMiddle,
        ButtonOrangeEnd
    ),
    center = androidx.compose.ui.geometry.Offset(0.87f, 0f)
)
