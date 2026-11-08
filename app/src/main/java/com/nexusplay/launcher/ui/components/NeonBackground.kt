package com.nexusplay.launcher.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nexusplay.launcher.ui.theme.Midnight
import com.nexusplay.launcher.ui.theme.NeonCyan
import com.nexusplay.launcher.ui.theme.NeonPurple
import com.nexusplay.launcher.ui.theme.Obsidian

/**
 * App-wide backdrop: dark gradient, faint tech grid (static layer) and two slowly drifting
 * neon glows (animated layer). The animated value is only read while drawing, so the
 * animation never triggers recomposition.
 */
@Composable
fun NeonBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val transition = rememberInfiniteTransition(label = "backdrop")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(16000, easing = LinearEasing), RepeatMode.Reverse),
        label = "drift"
    )

    Box(
        modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Obsidian, Midnight, Obsidian)))
    ) {
        // Static grid layer
        Box(
            Modifier
                .fillMaxSize()
                .drawBehind {
                    val step = 38.dp.toPx()
                    val line = Color.White.copy(alpha = 0.022f)
                    var x = 0f
                    while (x <= size.width) {
                        drawLine(line, Offset(x, 0f), Offset(x, size.height), 1f)
                        x += step
                    }
                    var y = 0f
                    while (y <= size.height) {
                        drawLine(line, Offset(0f, y), Offset(size.width, y), 1f)
                        y += step
                    }
                }
        )
        // Animated glow layer
        Box(
            Modifier
                .fillMaxSize()
                .drawBehind {
                    val w = size.width
                    val h = size.height
                    drawRect(
                        Brush.radialGradient(
                            listOf(NeonPurple.copy(alpha = 0.30f), Color.Transparent),
                            center = Offset(w * (0.10f + 0.30f * drift), h * 0.10f),
                            radius = w * 0.95f
                        )
                    )
                    drawRect(
                        Brush.radialGradient(
                            listOf(NeonCyan.copy(alpha = 0.20f), Color.Transparent),
                            center = Offset(w * (0.95f - 0.30f * drift), h * 0.92f),
                            radius = w * 0.90f
                        )
                    )
                }
        )
        content()
    }
}
