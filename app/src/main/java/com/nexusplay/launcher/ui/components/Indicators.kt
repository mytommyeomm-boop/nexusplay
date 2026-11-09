package com.nexusplay.launcher.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexusplay.launcher.ui.theme.Cyan
import com.nexusplay.launcher.ui.theme.Graphite0
import com.nexusplay.launcher.ui.theme.Graphite3
import com.nexusplay.launcher.ui.theme.Lime
import com.nexusplay.launcher.ui.theme.Titanium

/** Small indicator lamp. [pulse] makes it breathe; use it for at most one lamp per screen. */
@Composable
fun StatusLed(color: Color, modifier: Modifier = Modifier, lit: Boolean = true, pulse: Boolean = false) {
    if (pulse) PulsingLed(color, modifier) else LedCanvas(color, modifier, lit) { 1f }
}

@Composable
private fun PulsingLed(color: Color, modifier: Modifier) {
    val transition = rememberInfiniteTransition(label = "led")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ledAlpha"
    )
    LedCanvas(color, modifier, lit = true) { alpha }
}

@Composable
private fun LedCanvas(color: Color, modifier: Modifier, lit: Boolean, alpha: () -> Float) {
    Canvas(modifier.size(8.dp)) {
        val r = size.minDimension / 2f
        drawCircle(Graphite0, r + 1.dp.toPx())
        if (lit) {
            val a = alpha()
            drawCircle(color.copy(alpha = 0.22f * a), r * 2f)
            drawCircle(color.copy(alpha = a), r)
        } else {
            drawCircle(Graphite3, r)
        }
    }
}

/** Segmented level meter. [fraction] is read while drawing, so animating it never recomposes. */
@Composable
fun SegmentBar(
    fraction: () -> Float,
    modifier: Modifier = Modifier,
    color: Color = Lime,
    segments: Int = 20
) {
    Canvas(modifier.fillMaxWidth().height(10.dp)) {
        val gap = 3.dp.toPx()
        val w = (size.width - gap * (segments - 1)) / segments
        val lit = fraction().coerceIn(0f, 1f) * segments
        for (i in 0 until segments) {
            val x = i * (w + gap)
            drawRect(Graphite3, Offset(x, 0f), Size(w, size.height))
            val a = (lit - i).coerceIn(0f, 1f)
            if (a > 0f) drawRect(color.copy(alpha = a), Offset(x, 0f), Size(w, size.height))
        }
    }
}

/** Header underline: titanium hairline with a short lime tab. */
@Composable
fun HeaderRule(modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxWidth().height(3.dp)) {
        drawLine(Titanium.copy(alpha = 0.25f), Offset(0f, size.height / 2), Offset(size.width, size.height / 2), 1.dp.toPx())
        drawRect(Lime, Offset.Zero, Size(40.dp.toPx(), size.height))
    }
}

/** Spec-sheet line: label, dotted leader, monospace value. */
@Composable
fun Readout(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Cyan
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Text(label, style = MaterialTheme.typography.titleSmall, color = Titanium)
        Canvas(
            Modifier
                .weight(1f)
                .height(1.dp)
                .padding(horizontal = 8.dp)
                .offset(y = (-4).dp)
        ) {
            drawLine(
                Titanium.copy(alpha = 0.35f),
                Offset(0f, size.height / 2),
                Offset(size.width, size.height / 2),
                1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 5.dp.toPx()))
            )
        }
        Text(
            value,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp),
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
