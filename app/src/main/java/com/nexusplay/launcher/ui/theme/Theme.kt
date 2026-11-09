package com.nexusplay.launcher.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val NexusColors = darkColorScheme(
    primary = Lime,
    onPrimary = Graphite0,
    secondary = Cyan,
    onSecondary = Graphite0,
    tertiary = Orange,
    background = Graphite0,
    onBackground = TextPrimary,
    surface = Graphite1,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary
)

@Composable
fun NexusTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = NexusColors, typography = NexusTypography) {
        CompositionLocalProvider(LocalContentColor provides TextPrimary, content = content)
    }
}
