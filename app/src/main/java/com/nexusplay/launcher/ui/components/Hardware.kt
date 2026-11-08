package com.nexusplay.launcher.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.nexusplay.launcher.ui.theme.AccentLime
import com.nexusplay.launcher.ui.theme.Graphite0
import com.nexusplay.launcher.ui.theme.Graphite1
import com.nexusplay.launcher.ui.theme.Graphite2
import com.nexusplay.launcher.ui.theme.Hairline
import com.nexusplay.launcher.ui.theme.TextMuted
import com.nexusplay.launcher.ui.theme.TextSecondary
import com.nexusplay.launcher.ui.theme.Titanium

/** Machined corner cuts instead of rounded corners. Default is asymmetric (top-right + bottom-left). */
class ChamferShape(
    private val topLeft: Dp = 0.dp,
    private val topRight: Dp = 0.dp,
    private val bottomRight: Dp = 0.dp,
    private val bottomLeft: Dp = 0.dp
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val tl = with(density) { topLeft.toPx() }
        val tr = with(density) { topRight.toPx() }
        val br = with(density) { bottomRight.toPx() }
        val bl = with(density) { bottomLeft.toPx() }
        val w = size.width
        val h = size.height
        return Outline.Generic(
            Path().apply {
                moveTo(tl, 0f)
                lineTo(w - tr, 0f)
                lineTo(w, tr)
                lineTo(w, h - br)
                lineTo(w - br, h)
                lineTo(bl, h)
                lineTo(0f, h - bl)
                lineTo(0f, tl)
                close()
            }
        )
    }
}

/** Standard panel: graphite body, titanium top edge, small accent tab on the left seam. */
@Composable
fun HwPanel(
    modifier: Modifier = Modifier,
    accent: Color = AccentLime,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = ChamferShape(topRight = 16.dp, bottomLeft = 16.dp)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateFloatAsState(if (pressed) 1f else 0f, tween(80), label = "panelPress")

    val base = modifier
        .graphicsLayer { translationY = press * 1.5f * density }
        .clip(shape)
        .background(Brush.verticalGradient(listOf(Graphite2, Graphite1)))
        .border(1.dp, Brush.verticalGradient(listOf(Titanium.copy(alpha = 0.40f), Hairline)), shape)
        .drawBehind {
            drawRect(accent, Offset(0f, 16.dp.toPx()), Size(3.dp.toPx(), 20.dp.toPx()))
        }
    val interactive = if (onClick != null) {
        base.clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
    } else base

    Column(interactive.padding(start = 20.dp, top = 16.dp, end = 16.dp, bottom = 16.dp), content = content)
}

enum class KeyStyle { Primary, Secondary }

/** Mechanical key: solid ledge underneath that compresses when pressed. 52dp minimum height. */
@Composable
fun KeyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    style: KeyStyle = KeyStyle.Primary,
    accent: Color = AccentLime
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateFloatAsState(if (pressed && enabled) 1f else 0f, tween(70), label = "keyPress")
    val shape = ChamferShape(topRight = 12.dp, bottomLeft = 12.dp)
    val solid = enabled && style == KeyStyle.Primary
    val foreground = when {
        !enabled -> TextMuted
        style == KeyStyle.Primary -> Graphite0
        else -> accent
    }
    val borderColor = when {
        solid -> Color.Transparent
        enabled -> accent.copy(alpha = 0.6f)
        else -> Hairline
    }

    Row(
        modifier = modifier
            .heightIn(min = 52.dp)
            .graphicsLayer { translationY = press * 2f * density }
            .clip(shape)
            .background(if (solid) accent else if (enabled) Color.Transparent else Graphite2)
            .drawBehind {
                if (solid) {
                    val ledge = 4.dp.toPx() * (1f - press)
                    drawRect(Color.Black.copy(alpha = 0.25f), Offset(0f, size.height - ledge), Size(size.width, ledge))
                }
            }
            .border(1.dp, borderColor, shape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 22.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = foreground, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
        }
        Text(text.uppercase(), style = MaterialTheme.typography.labelLarge, color = foreground)
    }
}

/** Status indicator: square LED plus monospaced label. Use for functional / needs-runtime markers. */
@Composable
fun LampTag(text: String, color: Color = AccentLime, modifier: Modifier = Modifier) {
    val shape = ChamferShape(topRight = 6.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(Graphite0)
            .border(1.dp, Hairline, shape)
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(6.dp).background(color))
        Spacer(Modifier.width(7.dp))
        Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, color = TextSecondary, maxLines = 1)
    }
}

/** Screen heading: index code, big condensed title, one-line caption. */
@Composable
fun ScreenTitle(code: String, title: String, caption: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp)
            .staggeredEntrance(0),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(16.dp).height(2.dp).background(AccentLime))
            Spacer(Modifier.width(8.dp))
            Text(code, style = MaterialTheme.typography.labelSmall, color = Titanium)
        }
        Text(title.uppercase(), style = MaterialTheme.typography.displaySmall, color = com.nexusplay.launcher.ui.theme.TextPrimary)
        Text(caption, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

/** Section divider: monospaced caption with a seam line running to the edge. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, color = Titanium)
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f).height(1.dp).background(Hairline))
    }
}
