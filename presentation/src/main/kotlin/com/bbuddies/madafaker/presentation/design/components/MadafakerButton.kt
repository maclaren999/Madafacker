package com.bbuddies.madafaker.presentation.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.presentation.design.theme.MadafakerTheme


/**
 * Primary button with gradient background and custom styling
 */
@Composable
fun MadafakerPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val buttonShape = RoundedCornerShape(4.dp)
    val colorScheme = MaterialTheme.colorScheme
    val shadowColor = buttonShadow(colorScheme)
    val transparentSurface = colorScheme.surface.copy(alpha = 0f)

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(44.dp)
            .shadow(
                elevation = 2.dp,
                shape = buttonShape,
                spotColor = shadowColor,
                ambientColor = shadowColor
            )
            .clip(buttonShape)
            .background(
                brush = primaryButtonBackground(enabled, colorScheme)
            )
            .border(
                width = 1.dp,
                color = primaryBorder(enabled, colorScheme),
                shape = buttonShape
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = transparentSurface,
            contentColor = colorScheme.onPrimary,
            disabledContainerColor = transparentSurface,
            disabledContentColor = colorScheme.onSurfaceVariant
        ),
        shape = buttonShape,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            leadingIcon?.let {
                it()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * Secondary button with transparent background and border
 */
@Composable
fun MadafakerSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accentColor: Color? = null,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val buttonShape = RoundedCornerShape(4.dp)
    val colorScheme = MaterialTheme.colorScheme

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(44.dp)
            .border(
                width = 1.dp,
                color = secondaryBorder(enabled, colorScheme, accentColor),
                shape = buttonShape
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = secondaryContent(enabled, colorScheme, accentColor),
            disabledContainerColor = Color.Transparent,
            disabledContentColor = secondaryContent(
                enabled = false,
                colorScheme = colorScheme,
                accentColor = accentColor
            )
        ),
        shape = buttonShape,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            leadingIcon?.let {
                it()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * Text button without background
 */
@Composable
fun MadafakerTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accentColor: Color? = null,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val colorScheme = MaterialTheme.colorScheme

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(44.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = accentColor ?: colorScheme.onSurface,
            disabledContentColor = accentColor?.copy(alpha = 0.5f) ?: colorScheme.onSurfaceVariant
        ),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            leadingIcon?.let {
                it()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MadafakerPrimaryButtonPreview() {
    MadafakerTheme(Mode.SHINE) {
        MadafakerPrimaryButton(
            text = "Primary",
            onClick = {},
            enabled = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MadafakerSecondaryButtonPreview() {
    MadafakerTheme(Mode.SHINE) {
        MadafakerSecondaryButton(
            text = "Secondary",
            onClick = {},
            enabled = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MadafakerTextButtonPreview() {
    MadafakerTheme(Mode.SHADOW) {
        MadafakerTextButton(
            text = "Text",
            onClick = {},
            enabled = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MadafakerPrimaryButtonShadowPreview() {
    MadafakerTheme(Mode.SHADOW) {
        MadafakerPrimaryButton(
            text = "Primary",
            onClick = {},
            enabled = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MadafakerSecondaryButtonShadowPreview() {
    MadafakerTheme(Mode.SHADOW) {
        MadafakerSecondaryButton(
            text = "Secondary",
            onClick = {},
            enabled = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MadafakerTextButtonShinePreview() {
    MadafakerTheme(Mode.SHINE) {
        MadafakerTextButton(
            text = "Text",
            onClick = {},
            enabled = true
        )
    }
}

private fun primaryButtonBackground(enabled: Boolean, colorScheme: ColorScheme): Brush {
    if (!enabled) {
        return Brush.linearGradient(
            colors = listOf(
                colorScheme.primary,
                colorScheme.onPrimary
            )
        )
    }

    val primary = colorScheme.primary
    val highlight = colorScheme.primaryContainer
    val depth = primary.copy(alpha = 0.85f)

    return Brush.radialGradient(
        colors = listOf(
            highlight,
            primary,
            depth
        ),
        center = Offset(0.87f, 0f)
    )
}

private fun primaryBorder(enabled: Boolean, colorScheme: ColorScheme): Color =
    if (enabled) colorScheme.onBackground.copy(alpha = 0.8f) else colorScheme.outline

private fun secondaryBorder(
    enabled: Boolean,
    colorScheme: ColorScheme,
    accentColor: Color?
): Color {
    val activeColor = accentColor ?: colorScheme.onSurface
    val disabledColor = accentColor?.copy(alpha = 0.4f) ?: colorScheme.outline
    return if (enabled) activeColor else disabledColor
}

private fun secondaryContent(
    enabled: Boolean,
    colorScheme: ColorScheme,
    accentColor: Color?
): Color {
    val activeColor = accentColor ?: colorScheme.onSurface
    val disabledColor = accentColor?.copy(alpha = 0.5f) ?: colorScheme.onSurfaceVariant
    return if (enabled) activeColor else disabledColor
}

private fun buttonShadow(colorScheme: ColorScheme): Color =
    colorScheme.onBackground.copy(alpha = 0.45f)
