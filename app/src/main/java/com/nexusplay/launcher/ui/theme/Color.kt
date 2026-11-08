package com.nexusplay.launcher.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// --- Cyberdeck palette: graphite bodies, titanium trim, three signal accents ---
val Graphite0 = Color(0xFF0C0D0F)   // screen background
val Graphite1 = Color(0xFF131518)   // panel base
val Graphite2 = Color(0xFF1B1E23)   // raised surface
val Hairline = Color(0xFF2B3036)    // panel seams
val Titanium = Color(0xFF8D949C)
val TitaniumLight = Color(0xFFC5CAD0)

val AccentLime = Color(0xFFC6FF3D)    // primary action, selection
val AccentCyan = Color(0xFF35E0E8)    // info, live state
val AccentOrange = Color(0xFFFF7A1A)  // warning, recording, attention

val TextPrimary = Color(0xFFECEFF2)
val TextSecondary = Color(0xFFA2A9B1)
val TextMuted = Color(0xFF6C737B)

val Success = AccentLime
val Warning = AccentOrange

// --- Legacy names, remapped so screens not yet migrated still compile and match the new palette ---
val Obsidian = Graphite0
val Midnight = Graphite1
val Panel = Graphite2
val NeonCyan = AccentCyan
val NeonBlue = Color(0xFF1FA9B0)
val NeonPurple = AccentLime
val NeonPink = AccentOrange
val NeonGradient = Brush.horizontalGradient(listOf(AccentCyan, AccentLime))
