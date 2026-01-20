package com.bbuddies.madafaker.presentation.design.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.presentation.design.components.ModeBackground

private val DarkColorScheme = darkColorScheme(
    primary = ButtonOrangeEnd,
    secondary = DarkGray,
    tertiary = LightGray,
    background = Black,
    surface = Color(0xFF1C1C1C),
    onPrimary = White,
    onSecondary = White,
    onTertiary = Black,
    onBackground = White,
    onSurface = White,
    onSurfaceVariant = LightGray
)

private val LightColorScheme = lightColorScheme(
    primary = ButtonOrangeEnd,
    secondary = LightGray,
    tertiary = DarkGray,
    background = White,
    surface = White,
    onPrimary = White,
    onSecondary = Black,
    onTertiary = White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = DarkGray,
    outline = LightGray,
    surfaceVariant = Color(0xFFF5F5F5)
)

@Composable
fun MadafakerTheme(
    mode: Mode,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Determine dark theme based on mode
    // SHADOW mode = dark theme, SHINE mode = light theme
    // If mode is null, fall back to system theme
    val darkTheme = when (mode) {
        Mode.SHADOW -> true
        Mode.SHINE -> false
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            window.navigationBarColor = Color.Transparent.toArgb()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
            val useDarkNavBarIcons = colorScheme.onSurface.luminance() < 0.5f

            // Set status bar icons appearance
            // Light icons for dark theme, dark icons for light theme
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = false
        }
    }
    ModeBackground(
        mode = mode,
        modifier = Modifier.fillMaxSize()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }


}
