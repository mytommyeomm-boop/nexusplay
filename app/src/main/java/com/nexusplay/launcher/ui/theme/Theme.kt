package com.nexusplay.launcher.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val NexusColors = darkColorScheme(
    primary = AccentLime,
    onPrimary = Graphite0,
    secondary = AccentCyan,
    onSecondary = Graphite0,
    tertiary = AccentOrange,
    onTertiary = Graphite0,
    background = Graphite0,
    onBackground = TextPrimary,
    surface = Graphite1,
    onSurface = TextPrimary,
    surfaceVariant = Graphite2,
    onSurfaceVariant = TextSecondary,
    outline = Hairline
)

@Composable
fun NexusTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = NexusColors, typography = NexusTypography) {
        CompositionLocalProvider(LocalContentColor provides TextPrimary, content = content)
    }
}
