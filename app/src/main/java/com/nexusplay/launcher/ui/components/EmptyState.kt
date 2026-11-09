package com.nexusplay.launcher.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.nexusplay.launcher.ui.theme.Graphite0
import com.nexusplay.launcher.ui.theme.Lime
import com.nexusplay.launcher.ui.theme.Titanium

/** Recessed, dashed cartridge slot. Used for empty states and for shelves waiting for games. */
@Composable
fun SlotOutline(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    pins: Boolean = false
) {
    val shape = ChamferShape(14.dp, topRight = true)
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val path = shape.path(size, this)
            drawPath(path, Graphite0.copy(alpha = 0.55f))
            drawPath(
                path,
                Titanium.copy(alpha = 0.45f),
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx()))
                )
            )
            if (pins) {
                val pinW = 6.dp.toPx()
                val gap = 5.dp.toPx()
                val count = 10
                val total = count * pinW + (count - 1) * gap
                var x = (size.width - total) / 2f
                repeat(count) {
                    drawRect(
                        Titanium.copy(alpha = 0.28f),
                        Offset(x, size.height - 16.dp.toPx()),
                        Size(pinW, 8.dp.toPx())
                    )
                    x += pinW + gap
                }
            }
        }
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = Titanium, modifier = Modifier.size(30.dp))
        }
    }
}

/** An empty screen is an invitation: it always carries the next action. */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    DeckPanel(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = ChamferShape(22.dp, topLeft = true, bottomRight = true),
        screws = setOf(Corner.TopRight, Corner.BottomLeft),
        contentPadding = PaddingValues(18.dp)
    ) {
        Box(Modifier.fillMaxWidth().height(124.dp)) {
            SlotOutline(Modifier.fillMaxSize(), icon = icon, pins = true)
            StatusLed(Lime, Modifier.align(Alignment.TopStart).padding(14.dp), pulse = true)
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = Titanium)
        Spacer(Modifier.height(18.dp))
        DeckButton(text = actionLabel, onClick = onAction)
    }
}
