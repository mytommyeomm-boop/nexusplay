package com.nexusplay.launcher.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import com.nexusplay.launcher.ui.theme.AccentLime
import com.nexusplay.launcher.ui.theme.Graphite0
import com.nexusplay.launcher.ui.theme.Graphite2
import com.nexusplay.launcher.ui.theme.Hairline
import com.nexusplay.launcher.ui.theme.TextMuted
import com.nexusplay.launcher.ui.theme.TextPrimary
import com.nexusplay.launcher.ui.theme.Titanium

/** Slider with a machined track, tick marks and a rectangular grip. Horizontal drags only, so lists still scroll. */
@Composable
fun HwSlider(
    label: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    step: Float = 0.01f,
    enabled: Boolean = true,
    note: String? = null
) {
    val currentOnChange by rememberUpdatedState(onChange)
    val currentRange by rememberUpdatedState(range)
    val currentStep by rememberUpdatedState(step)
    var widthPx by remember { mutableIntStateOf(0) }
    val padPx = with(LocalDensity.current) { 12.dp.toPx() }

    fun fromX(x: Float) {
        val usable = (widthPx - 2 * padPx).coerceAtLeast(1f)
        val frac = ((x - padPx) / usable).coerceIn(0f, 1f)
        var v = currentRange.start + frac * (currentRange.endInclusive - currentRange.start)
        if (currentStep > 0f) v = (v / currentStep).roundToInt() * currentStep
        currentOnChange(v.coerceIn(currentRange.start, currentRange.endInclusive))
    }

    Column(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                label, style = MaterialTheme.typography.titleSmall,
                color = if (enabled) TextPrimary else TextMuted, modifier = Modifier.weight(1f)
            )
            Text(
                valueText, style = MaterialTheme.typography.labelLarge,
                color = if (enabled) AccentLime else TextMuted
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(40.dp)
                .onSizeChanged { widthPx = it.width }
                .pointerInput(enabled) {
                    if (enabled) detectTapGestures(onTap = { fromX(it.x) })
                }
                .pointerInput(enabled) {
                    if (enabled) detectHorizontalDragGestures(onDragStart = { fromX(it.x) }) { change, _ ->
                        change.consume(); fromX(change.position.x)
                    }
                }
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val pad = 12.dp.toPx()
                val cy = size.height / 2f
                val left = pad
                val right = size.width - pad
                val frac = ((value - range.start) / (range.endInclusive - range.start)).coerceIn(0f, 1f)
                val gx = left + (right - left) * frac
                val accent = if (enabled) AccentLime else TextMuted
                drawRect(Hairline, Offset(left, cy - 2.dp.toPx()), Size(right - left, 4.dp.toPx()))
                for (i in 0..10) {
                    val x = left + (right - left) * i / 10f
                    val h = if (i % 5 == 0) 7.dp.toPx() else 4.dp.toPx()
                    drawLine(Titanium.copy(alpha = 0.35f), Offset(x, cy + 8.dp.toPx()), Offset(x, cy + 8.dp.toPx() + h), 1f)
                }
                drawRect(accent.copy(alpha = 0.9f), Offset(left, cy - 2.dp.toPx()), Size(gx - left, 4.dp.toPx()))
                val gw = 12.dp.toPx(); val gh = 24.dp.toPx()
                drawRect(Graphite2, Offset(gx - gw / 2, cy - gh / 2), Size(gw, gh))
                drawRect(
                    if (enabled) Titanium else Hairline, Offset(gx - gw / 2, cy - gh / 2), Size(gw, gh),
                    style = Stroke(1.5.dp.toPx())
                )
                drawLine(accent, Offset(gx, cy - 6.dp.toPx()), Offset(gx, cy + 6.dp.toPx()), 2.dp.toPx())
            }
        }
        if (note != null) Text(note, style = MaterialTheme.typography.bodySmall, color = TextMuted)
    }
}

/** Toggle row with a sliding lever. The whole row is the touch target. */
@Composable
fun HwToggleRow(
    label: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    caption: String? = null,
    enabled: Boolean = true
) {
    val shape = ChamferShape(topRight = 6.dp)
    val anim by animateFloatAsState(if (checked) 1f else 0f, tween(140), label = "toggle")
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .toggleable(
                value = checked, enabled = enabled, role = Role.Switch,
                interactionSource = remember { MutableInteractionSource() }, indication = null,
                onValueChange = onChange
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f).padding(vertical = 6.dp)) {
            Text(label, style = MaterialTheme.typography.titleSmall, color = if (enabled) TextPrimary else TextMuted)
            if (caption != null) Text(caption, style = MaterialTheme.typography.bodySmall, color = TextMuted)
        }
        Spacer(Modifier.width(12.dp))
        val accent = if (enabled) AccentLime else TextMuted
        Box(
            Modifier
                .size(width = 48.dp, height = 26.dp)
                .clip(shape)
                .background(if (checked && enabled) AccentLime.copy(alpha = 0.16f) else Graphite0)
                .border(1.dp, if (checked && enabled) AccentLime.copy(alpha = 0.6f) else Hairline, shape)
                .drawBehind {
                    val lw = 18.dp.toPx()
                    val x = 4.dp.toPx() + (size.width - lw - 8.dp.toPx()) * anim
                    drawRect(if (checked) accent else Titanium, Offset(x, (size.height - lw) / 2f), Size(lw, lw))
                }
        )
    }
}

/** Horizontal strip of exclusive choices; scrolls sideways when the labels do not fit. */
@Composable
fun HwSegmented(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEachIndexed { i, text ->
            val on = i == selected
            val shape = ChamferShape(topRight = 8.dp)
            Box(
                Modifier
                    .width(82.dp)
                    .height(44.dp)
                    .clip(shape)
                    .background(if (on) AccentLime else Graphite2)
                    .border(1.dp, if (on) Color.Transparent else Hairline, shape)
                    .clickable(role = Role.RadioButton, onClick = { onSelect(i) }),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text.uppercase(), style = MaterialTheme.typography.labelSmall,
                    color = if (on) Graphite0 else Titanium, maxLines = 1, overflow = TextOverflow.Clip
                )
            }
        }
    }
}

/** Compact key for action rows. 44dp tall. */
@Composable
fun SmallKey(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accent: Color = AccentLime
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateFloatAsState(if (pressed && enabled) 1f else 0f, tween(70), label = "smallKey")
    val shape = ChamferShape(topRight = 8.dp)
    Box(
        modifier
            .height(44.dp)
            .graphicsLayer { translationY = press * 2f * density }
            .clip(shape)
            .background(if (pressed && enabled) accent.copy(alpha = 0.14f) else Graphite0)
            .border(1.dp, if (enabled) accent.copy(alpha = 0.55f) else Hairline, shape)
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text.uppercase(), style = MaterialTheme.typography.labelSmall,
            color = if (enabled) accent else TextMuted, maxLines = 1
        )
    }
}
