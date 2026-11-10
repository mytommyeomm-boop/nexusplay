package com.nexusplay.launcher.ui.theme

import android.graphics.Typeface
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Thai note: no letter-spacing anywhere (it breaks Thai tone-mark placement) and tall line boxes so
 * stacked vowels and tone marks are never clipped. Thai glyphs come from the system Thai font via fallback.
 * System condensed face for hardware-label headings, monospace for readouts, sans for reading. */
private val Condensed = FontFamily(Typeface.create("sans-serif-condensed", Typeface.BOLD))
private val Body = FontFamily.SansSerif
val Mono = FontFamily.Monospace

val NexusTypography = Typography(
    displaySmall = TextStyle(fontFamily = Condensed, fontSize = 42.sp, lineHeight = 58.sp, letterSpacing = 0.sp),
    headlineMedium = TextStyle(fontFamily = Condensed, fontSize = 28.sp, lineHeight = 40.sp, letterSpacing = 0.sp),
    titleMedium = TextStyle(fontFamily = Condensed, fontSize = 20.sp, lineHeight = 30.sp, letterSpacing = 0.sp),
    titleSmall = TextStyle(fontFamily = Condensed, fontSize = 16.sp, lineHeight = 26.sp, letterSpacing = 0.sp),
    bodyMedium = TextStyle(fontFamily = Body, fontSize = 14.sp, lineHeight = 24.sp),
    bodySmall = TextStyle(fontFamily = Body, fontSize = 12.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontFamily = Condensed, fontSize = 17.sp, lineHeight = 26.sp, letterSpacing = 0.sp),
    labelSmall = TextStyle(fontFamily = Mono, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 18.sp, letterSpacing = 0.sp)
)
