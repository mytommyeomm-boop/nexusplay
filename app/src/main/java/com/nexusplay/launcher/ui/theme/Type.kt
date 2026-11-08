package com.nexusplay.launcher.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Family = FontFamily.SansSerif

val NexusTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = Family, fontWeight = FontWeight.Black,
        fontSize = 32.sp, lineHeight = 36.sp, letterSpacing = 0.5.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Family, fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp, lineHeight = 28.sp, letterSpacing = 0.4.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Family, fontWeight = FontWeight.Bold,
        fontSize = 17.sp, lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Family, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 18.sp, letterSpacing = 0.2.sp
    ),
    bodyMedium = TextStyle(fontFamily = Family, fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontFamily = Family, fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(
        fontFamily = Family, fontWeight = FontWeight.Bold,
        fontSize = 14.sp, letterSpacing = 0.6.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Family, fontWeight = FontWeight.Bold,
        fontSize = 11.sp, letterSpacing = 0.8.sp
    )
)
