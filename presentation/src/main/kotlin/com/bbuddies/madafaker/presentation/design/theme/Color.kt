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

// Button Gradient
val ButtonGradient = Brush.radialGradient(
    colors = listOf(
        ButtonOrangeStart,
        ButtonOrangeMiddle,
        ButtonOrangeEnd
    ),
    center = androidx.compose.ui.geometry.Offset(0.87f, 0f),
    radius = 1.0f
)
