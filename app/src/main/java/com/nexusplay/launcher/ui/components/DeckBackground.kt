package com.nexusplay.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nexusplay.launcher.ui.theme.Graphite0

/**
 * Brushed-metal backdrop. Fully static (nothing animates here), so it costs
 * almost nothing after the first frame.
 */
@Composable
fun DeckBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Graphite0)
            .drawBehind {
                val step = 3.dp.toPx()
                var y = 0f
                var i = 0
                while (y < size.height) {
                    if (i % 2 == 0) {
                        drawRect(Color.White.copy(alpha = 0.014f), Offset(0f, y), Size(size.width, step * 0.5f))
                    }
                    y += step
                    i++
                }
                drawRect(
                    Brush.radialGradient(
                        listOf(Color.White.copy(alpha = 0.07f), Color.Transparent),
                        center = Offset(0f, 0f),
                        radius = size.width * 1.1f
                    )
                )
                drawRect(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f)),
                        startY = size.height * 0.55f,
                        endY = size.height
                    )
                )
            },
        content = content
    )
}
