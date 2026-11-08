package com.nexusplay.launcher.ui.theme

import android.graphics.Typeface
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// System fonts only (no font files, no new dependency): condensed bold for display,
// monospace for instrument-style labels, default sans for reading text.
val DisplayFamily = FontFamily(Typeface.create("sans-serif-condensed", Typeface.BOLD))
val LabelFamily = FontFamily.Monospace
private val BodyFamily = FontFamily.SansSerif

val NexusTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = DisplayFamily, fontSize = 36.sp, lineHeight = 38.sp, letterSpacing = 0.5.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = DisplayFamily, fontSize = 26.sp, lineHeight = 30.sp, letterSpacing = 0.4.sp
    ),
    titleMedium = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp
    ),
    bodyMedium = TextStyle(fontFamily = BodyFamily, fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontFamily = BodyFamily, fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(
        fontFamily = LabelFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 1.2.sp
    ),
    labelSmall = TextStyle(
        fontFamily = LabelFamily, fontWeight = FontWeight.Medium, fontSize = 10.5.sp, letterSpacing = 1.4.sp
    )
)
