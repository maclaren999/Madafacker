package com.bbuddies.madafaker.presentation.design.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.bbuddies.madafaker.presentation.design.theme.LightGray

/**
 * Custom TextField with hand-drawn underline styling
 */
@Composable
fun MadafakerTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    label: String = "",
    enabled: Boolean = true,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    supportingText: @Composable (() -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        // Label
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Text Field Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 4.dp)
                    .onFocusChanged { isFocused = it.isFocused },
                enabled = enabled,
                textStyle = textStyle.copy(
                    color = MaterialTheme.colorScheme.onBackground
                ),
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                singleLine = singleLine,
                maxLines = maxLines,
                visualTransformation = visualTransformation,
                decorationBox = { innerTextField ->
                    Box {
                        if (value.isEmpty() && placeholder.isNotEmpty()) {
                            Text(
                                text = placeholder,
                                style = textStyle,
                                color = LightGray
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        // Hand-drawn underline
        HandDrawnUnderline(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            isActive = isFocused,
            enabled = enabled
        )

        // Supporting text
        supportingText?.let {
            Box(modifier = Modifier.padding(top = 4.dp)) {
                it()
            }
        }
    }
}

/**
 * Hand-drawn underline component based on the SVG path
 */
@Composable
private fun HandDrawnUnderline(
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    enabled: Boolean = true
) {
    val color = MaterialTheme.colorScheme.onBackground

    Canvas(modifier = modifier) {
        drawHandDrawnPath(color)
    }
}

/**
 * Draws the hand-drawn underline path based on the provided SVG
 */
private fun DrawScope.drawHandDrawnPath(color: Color) {
    val path = Path().apply {
        // Scale the original SVG path (332x8) to fit the available width
        val scaleX = size.width / 332f
        val scaleY = size.height / 8f

        // Original SVG path converted to Compose Path
        moveTo(52.8027f * scaleX, 7.90472f * scaleY)
        cubicTo(
            8.06364f * scaleX, 8.07206f * scaleY,
            0.687127f * scaleX, 8.03701f * scaleY,
            0f * scaleX, 7.65429f * scaleY
        )
        lineTo(5.00245f * scaleX, 5.35211f * scaleY)
        cubicTo(
            12.7789f * scaleX, 4.55165f * scaleY,
            31.7004f * scaleX, 4.06701f * scaleY,
            95.4127f * scaleX, 3.03631f * scaleY
        )
        cubicTo(
            116.818f * scaleX, 2.69016f * scaleY,
            159.697f * scaleX, 1.91913f * scaleY,
            190.698f * scaleX, 1.32289f * scaleY
        )
        cubicTo(
            221.699f * scaleX, 0.726659f * scaleY,
            256.425f * scaleX, 0.155542f * scaleY,
            267.866f * scaleX, 0.0535876f * scaleY
        )
        cubicTo(
            291.765f * scaleX, -0.158999f * scaleY,
            314.202f * scaleX, 0.282886f * scaleY,
            315.426f * scaleX, 0.990682f * scaleY
        )
        cubicTo(
            315.905f * scaleX, 1.26742f * scaleY,
            318.253f * scaleX, 1.50042f * scaleY,
            321.883f * scaleX, 1.6315f * scaleY
        )
        cubicTo(
            328.93f * scaleX, 1.88623f * scaleY,
            332.019f * scaleX, 2.1757f * scaleY,
            332f * scaleX, 2.57949f * scaleY
        )
        cubicTo(
            331.973f * scaleX, 3.15465f * scaleY,
            322.308f * scaleX, 3.39762f * scaleY,
            305.443f * scaleX, 3.24702f * scaleY
        )
        cubicTo(
            290.874f * scaleX, 3.11686f * scaleY,
            284.563f * scaleX, 3.23432f * scaleY,
            257.465f * scaleX, 4.14076f * scaleY
        )
        cubicTo(
            176.098f * scaleX, 6.86224f * scaleY,
            136.442f * scaleX, 7.59172f * scaleY,
            52.8027f * scaleX, 7.90472f * scaleY
        )
        close()
    }

    drawPath(
        path = path,
        color = color
    )
}
