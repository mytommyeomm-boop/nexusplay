package com.nexusplay.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nexusplay.launcher.ui.theme.AccentCyan
import com.nexusplay.launcher.ui.theme.Graphite0

/**
 * Static backdrop: faint machined horizontal grain, a soft top light and one cool corner light.
 * Drawn once and cached; there is deliberately no infinite animation here.
 */
@Composable
fun HwBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier
            .fillMaxSize()
            .background(Graphite0)
            .drawWithCache {
                val top = Brush.verticalGradient(
                    listOf(Color(0xFF181B1F), Graphite0), endY = size.height * 0.55f
                )
                val corner = Brush.radialGradient(
                    listOf(AccentCyan.copy(alpha = 0.07f), Color.Transparent),
                    center = Offset(size.width, 0f),
                    radius = size.width * 0.9f
                )
                val step = 4.dp.toPx()
                val grain = Color.White.copy(alpha = 0.014f)
                onDrawBehind {
                    drawRect(top)
                    drawRect(corner)
                    var y = 0f
                    while (y <= size.height) {
                        drawLine(grain, Offset(0f, y), Offset(size.width, y), 1f)
                        y += step
                    }
                }
            },
        content = content
    )
}
