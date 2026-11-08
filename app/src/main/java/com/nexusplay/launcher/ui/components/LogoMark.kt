package com.nexusplay.launcher.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import com.nexusplay.launcher.ui.theme.NeonCyan
import com.nexusplay.launcher.ui.theme.NeonPurple
import kotlin.math.cos
import kotlin.math.sin

/** Hexagon + play glyph. Matches the launcher icon. */
@Composable
fun LogoMark(dimension: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(dimension)) {
        val c = center
        val r = size.minDimension / 2f * 0.86f
        val hex = Path().apply {
            for (i in 0 until 6) {
                val a = Math.toRadians((-90 + 60 * i).toDouble())
                val x = c.x + r * cos(a).toFloat()
                val y = c.y + r * sin(a).toFloat()
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        val gradient = Brush.linearGradient(
            colors = listOf(NeonCyan, NeonPurple),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height)
        )
        drawPath(hex, gradient, style = Stroke(width = size.minDimension * 0.07f, join = StrokeJoin.Round))
        val play = Path().apply {
            moveTo(c.x - r * 0.22f, c.y - r * 0.38f)
            lineTo(c.x - r * 0.22f, c.y + r * 0.38f)
            lineTo(c.x + r * 0.42f, c.y)
            close()
        }
        drawPath(play, gradient)
    }
}
