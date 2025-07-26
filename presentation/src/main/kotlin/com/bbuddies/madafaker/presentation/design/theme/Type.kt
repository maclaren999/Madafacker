package com.bbuddies.madafaker.presentation.design.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.bbuddies.madafaker.presentation.R


// Font families
val MontserratSemiBold = FontFamily(
    Font(R.font.montserrat_semibold, FontWeight.SemiBold)
)

val OpenSansRegular = FontFamily(
    Font(R.font.open_sans, FontWeight.Normal)
)

val OpenSansSemiBold = FontFamily(
    Font(R.font.open_sans_semibold, FontWeight.SemiBold)
)

val OpenSansBold = FontFamily(
    Font(R.font.open_sans_bold, FontWeight.Bold)
)

// Custom typography based on design system
val Typography = Typography(
    // H1 - Montserrat SemiBold, 40sp, 150% line height, 0% spacing
    headlineLarge = TextStyle(
        fontFamily = MontserratSemiBold,
        fontWeight = FontWeight.SemiBold,
        fontSize = 40.sp,
        lineHeight = 60.sp, // 150% of 40sp
        letterSpacing = 0.sp,
        color = TextPrimary
    ),

    // H2 - Open Sans SemiBold, 18sp, 150% line height, 0% spacing
    headlineMedium = TextStyle(
        fontFamily = OpenSansSemiBold,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 27.sp, // 150% of 18sp
        letterSpacing = 0.sp
    ),

    // Main text - Open Sans Regular, 16sp, 150% line height, 0% spacing
    bodyLarge = TextStyle(
        fontFamily = OpenSansRegular,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp, // 150% of 16sp
        letterSpacing = 0.sp
    ),

    // Button - Open Sans Bold, 16sp, 150% line height, 0% spacing
    labelLarge = TextStyle(
        fontFamily = OpenSansBold,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 24.sp, // 150% of 16sp
        letterSpacing = 0.sp
    ),

    // Additional common text styles using the same fonts
    bodyMedium = TextStyle(
        fontFamily = OpenSansRegular,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp, // 150% of 14sp
        letterSpacing = 0.sp
    ),

    bodySmall = TextStyle(
        fontFamily = OpenSansRegular,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp, // 150% of 12sp
        letterSpacing = 0.sp
    ),

    headlineSmall = TextStyle(
        fontFamily = OpenSansSemiBold,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp, // 150% of 16sp
        letterSpacing = 0.sp
    ),

    titleLarge = TextStyle(
        fontFamily = OpenSansSemiBold,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 33.sp, // 150% of 22sp
        letterSpacing = 0.sp
    ),

    titleMedium = TextStyle(
        fontFamily = OpenSansSemiBold,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp, // 150% of 16sp
        letterSpacing = 0.sp
    ),

    labelMedium = TextStyle(
        fontFamily = OpenSansBold,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 21.sp, // 150% of 14sp
        letterSpacing = 0.sp
    ),

    labelSmall = TextStyle(
        fontFamily = OpenSansBold,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 18.sp, // 150% of 12sp
        letterSpacing = 0.sp
    )
)