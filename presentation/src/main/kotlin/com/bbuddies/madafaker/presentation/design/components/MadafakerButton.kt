package com.bbuddies.madafaker.presentation.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bbuddies.madafaker.presentation.design.theme.ButtonGradient
import com.bbuddies.madafaker.presentation.design.theme.ButtonOrangeEnd
import com.bbuddies.madafaker.presentation.design.theme.TextPrimary
import com.bbuddies.madafaker.presentation.design.theme.White
import androidx.compose.ui.tooling.preview.Preview
import com.bbuddies.madafaker.presentation.design.theme.LightGray


/**
 * Primary button with gradient background and custom styling
 */
@Composable
fun MadafakerPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val buttonShape = RoundedCornerShape(4.dp)

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(44.dp)
            .shadow(
                elevation = 2.dp,
                shape = buttonShape,
                spotColor = Color(0x40131313),
                ambientColor = Color(0x40131313)
            )
            .clip(buttonShape)
            .background(
                brush = if (enabled) ButtonGradient else Brush.linearGradient(
                    colors = listOf(Color(0xFFE0E0E0), Color(0xFFBDBDBD))
                )
            )
            .border(
                width = 1.dp,
                color = if (enabled) Color(0xFF000000) else Color(0xFF9E9E9E),
                shape = buttonShape
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = if (enabled) White else Color(0xFF757575),
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color(0xFF757575)
        ),
        shape = buttonShape,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
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
    enabled: Boolean = true
) {
    val buttonShape = RoundedCornerShape(4.dp)

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(44.dp)
            .border(
                width = 1.dp,
                color = if (enabled) TextPrimary else LightGray,
                shape = buttonShape
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = if (enabled) TextPrimary else LightGray,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = LightGray
        ),
        shape = buttonShape,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
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
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(44.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (enabled)  TextPrimary else LightGray,
            disabledContentColor =LightGray
        ),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MadafakerPrimaryButtonPreview() {
    MaterialTheme {
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
    MaterialTheme {

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
    MaterialTheme {
        MadafakerTextButton(
            text = "Text",
            onClick = {},
            enabled = true
        )
    }
}

