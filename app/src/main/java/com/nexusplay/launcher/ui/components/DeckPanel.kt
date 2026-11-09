package com.nexusplay.launcher.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.nexusplay.launcher.ui.theme.Graphite0
import com.nexusplay.launcher.ui.theme.Graphite1
import com.nexusplay.launcher.ui.theme.Graphite2
import com.nexusplay.launcher.ui.theme.Graphite3
import com.nexusplay.launcher.ui.theme.Titanium

/**
 * Machined plate: chamfered silhouette, beveled edge, top highlight and optional screw heads.
 * When [onClick] is set the plate physically sinks 2dp while pressed.
 */
@Composable
fun DeckPanel(
    modifier: Modifier = Modifier,
    shape: ChamferShape = ChamferShape(18.dp, topLeft = true, bottomRight = true),
    screws: Set<Corner> = emptySet(),
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val sink by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 600f),
        label = "panelSink"
    )

    val base = modifier
        .graphicsLayer { translationY = sink * 2.dp.toPx() }
        .clip(shape)
        .background(Brush.verticalGradient(listOf(Graphite2, Graphite1)))
        .drawBehind {
            drawLine(
                Color.White.copy(alpha = 0.08f),
                Offset(20.dp.toPx(), 1f),
                Offset(size.width - 20.dp.toPx(), 1f),
                2f
            )
            val inset = 11.dp.toPx()
            screws.forEach { corner ->
                val x = if (corner == Corner.TopLeft || corner == Corner.BottomLeft) inset else size.width - inset
                val y = if (corner == Corner.TopLeft || corner == Corner.TopRight) inset else size.height - inset
                drawScrew(Offset(x, y))
            }
            if (sink > 0f) drawRect(Color.Black.copy(alpha = 0.2f * sink))
        }
        .border(
            1.dp,
            Brush.linearGradient(
                listOf(Titanium.copy(alpha = 0.5f), Graphite3, Titanium.copy(alpha = 0.12f))
            ),
            shape
        )

    val interactive = if (onClick != null) {
        base.clickable(
            interactionSource = interaction,
            indication = null,
            role = Role.Button,
            onClick = onClick
        )
    } else base

    Column(interactive.padding(contentPadding), content = content)
}

private fun DrawScope.drawScrew(center: Offset) {
    val r = 3.5.dp.toPx()
    drawCircle(Graphite0, r, center)
    drawCircle(Titanium.copy(alpha = 0.5f), r, center, style = Stroke(1f))
    drawLine(
        Titanium.copy(alpha = 0.6f),
        Offset(center.x - r * 0.6f, center.y),
        Offset(center.x + r * 0.6f, center.y),
        1f
    )
}
