package com.nexusplay.launcher.ui.components

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexusplay.launcher.ui.theme.Cyan
import com.nexusplay.launcher.ui.theme.Graphite0
import com.nexusplay.launcher.ui.theme.Graphite2
import com.nexusplay.launcher.ui.theme.Graphite3
import com.nexusplay.launcher.ui.theme.Lime
import com.nexusplay.launcher.ui.theme.TextMuted
import com.nexusplay.launcher.ui.theme.TextPrimary
import com.nexusplay.launcher.ui.theme.Titanium
import com.nexusplay.launcher.ui.theme.TitaniumLight
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Hardware-style slider: grooved track, tick marks, chamfered thumb. 44dp tall touch target. */
@Composable
fun DeckSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier
) {
    val change by rememberUpdatedState(onValueChange)
    val span = range.endInclusive - range.start
    val fraction = ((value - range.start) / span).coerceIn(0f, 1f)

    Box(
        modifier
            .fillMaxWidth()
            .height(44.dp)
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(value.coerceIn(range.start, range.endInclusive), range)
                setProgress { target ->
                    change(target.coerceIn(range.start, range.endInclusive))
                    true
                }
            }
            .pointerInput(range) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val pad = 14.dp.toPx()
                    fun toValue(x: Float): Float {
                        val f = ((x - pad) / (size.width - 2 * pad)).coerceIn(0f, 1f)
                        return range.start + f * span
                    }
                    change(toValue(down.position.x))
                    down.consume()
                    while (true) {
                        val event = awaitPointerEvent()
                        val c = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!c.pressed) break
                        change(toValue(c.position.x))
                        c.consume()
                    }
                }
            }
            .drawBehind {
                val pad = 14.dp.toPx()
                val trackH = 6.dp.toPx()
                val cy = size.height / 2f
                val left = pad
                val right = size.width - pad
                val tx = left + (right - left) * fraction

                drawRect(Graphite0, Offset(left, cy - trackH / 2), Size(right - left, trackH))
                drawRect(Graphite3, Offset(left, cy - trackH / 2), Size(right - left, trackH), style = Stroke(1f))
                drawRect(Lime, Offset(left, cy - trackH / 2), Size(tx - left, trackH))
                for (i in 0..10) {
                    val x = left + (right - left) * i / 10f
                    drawLine(Titanium.copy(alpha = 0.35f), Offset(x, cy + 9.dp.toPx()), Offset(x, cy + 13.dp.toPx()), 1f)
                }
                val tw = 14.dp.toPx()
                val th = 28.dp.toPx()
                val thumb = ChamferShape(5.dp, topLeft = true, bottomRight = true).path(Size(tw, th), this)
                translate(tx - tw / 2f, cy - th / 2f) {
                    drawPath(thumb, Brush.verticalGradient(listOf(TitaniumLight, Titanium), startY = 0f, endY = th))
                    drawLine(Lime, Offset(3.dp.toPx(), th / 2f), Offset(tw - 3.dp.toPx(), th / 2f), 2.dp.toPx())
                }
            }
    )
}

/** Small mechanical key. With [repeat] it keeps firing while held, for fine +/- adjustment. */
@Composable
fun KeyButton(
    label: String,
    onPress: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp? = null,
    repeat: Boolean = false,
    enabled: Boolean = true,
    highlight: Boolean = false
) {
    val press by rememberUpdatedState(onPress)
    var down by remember { mutableStateOf(false) }
    val travel by animateFloatAsState(
        targetValue = if (down) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 800f),
        label = "keyButton"
    )
    val shape = ChamferShape(8.dp, topLeft = true, bottomRight = true)

    Box(
        modifier = modifier
            .then(if (width != null) Modifier.width(width) else Modifier.widthIn(min = 44.dp))
            .heightIn(min = 44.dp)
            .graphicsLayer { translationY = travel * 2.dp.toPx() }
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Graphite3, Graphite2)))
            .border(1.dp, if (highlight) Lime.copy(alpha = 0.7f) else Titanium.copy(alpha = 0.25f), shape)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                if (!enabled) disabled()
                onClick(label) {
                    if (enabled) press()
                    enabled
                }
            }
            .pointerInput(enabled, repeat) {
                if (!enabled) return@pointerInput
                detectTapGestures(onPress = {
                    down = true
                    coroutineScope {
                        val job = launch(start = CoroutineStart.UNDISPATCHED) {
                            press()
                            if (repeat) {
                                delay(380)
                                while (true) {
                                    press()
                                    delay(70)
                                }
                            }
                        }
                        tryAwaitRelease()
                        job.cancel()
                    }
                    down = false
                })
            }
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleSmall,
            color = if (enabled) TextPrimary else TextMuted,
            maxLines = 2,
            textAlign = TextAlign.Center
        )
    }
}

/** Slider with a label, a live value readout and optional fine -/+ keys. */
@Composable
fun SliderRow(
    label: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
    step: Float? = null
) {
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.titleSmall, color = Titanium, modifier = Modifier.weight(1f))
            Text(valueText, style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp), color = Cyan)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            DeckSlider(value, onChange, range, Modifier.weight(1f))
            if (step != null) {
                Spacer(Modifier.width(8.dp))
                KeyButton("\u2212", { onChange((value - step).coerceIn(range.start, range.endInclusive)) }, repeat = true)
                Spacer(Modifier.width(6.dp))
                KeyButton("+", { onChange((value + step).coerceIn(range.start, range.endInclusive)) }, repeat = true)
            }
        }
    }
}

/** Mechanical on/off switch; the whole row is the touch target. */
@Composable
fun ToggleRow(
    label: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    caption: String? = null,
    enabled: Boolean = true
) {
    val pos by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 600f),
        label = "toggle"
    )
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                interactionSource = interaction,
                indication = null,
                onValueChange = onChange
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleSmall, color = if (enabled) TextPrimary else TextMuted)
            if (caption != null) Text(caption, style = MaterialTheme.typography.bodySmall, color = if (enabled) Titanium else TextMuted)
        }
        Spacer(Modifier.width(12.dp))
        Canvas(Modifier.size(52.dp, 26.dp)) {
            val track = ChamferShape(7.dp, topLeft = true, bottomRight = true).path(size, this)
            drawPath(track, Graphite0)
            if (pos > 0f) drawPath(track, Lime.copy(alpha = 0.22f * pos))
            drawPath(track, Titanium.copy(alpha = (0.35f + 0.4f * pos) * (if (enabled) 1f else 0.5f)), style = Stroke(1.dp.toPx()))
            val kw = 20.dp.toPx()
            val kh = size.height - 6.dp.toPx()
            val kx = 3.dp.toPx() + (size.width - kw - 6.dp.toPx()) * pos
            val knob = ChamferShape(4.dp, topLeft = true, bottomRight = true).path(Size(kw, kh), this)
            translate(kx, 3.dp.toPx()) {
                drawPath(knob, lerp(Titanium, Lime, pos).copy(alpha = if (enabled) 1f else 0.4f))
            }
        }
    }
}

/** Row of keys where exactly one is selected. The active key sits recessed under a lime tab. */
@Composable
fun DeckSegmented(
    options: List<String>,
    selected: Int,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit
) {
    val shape = ChamferShape(10.dp, topLeft = true, bottomRight = true)
    Row(
        modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 46.dp)
            .height(IntrinsicSize.Min)
            .clip(shape)
            .background(Graphite0)
            .border(1.dp, Graphite3, shape)
            .padding(3.dp)
    ) {
        options.forEachIndexed { index, label ->
            val on = index == selected
            val bg by animateColorAsState(if (on) Graphite3 else Color.Transparent, tween(140), label = "segment")
            val interaction = remember { MutableInteractionSource() }
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(bg)
                    .drawBehind {
                        if (on) {
                            drawRect(Lime, Offset((size.width - 20.dp.toPx()) / 2f, 0f), Size(20.dp.toPx(), 2.dp.toPx()))
                        }
                    }
                    .selectable(
                        selected = on,
                        interactionSource = interaction,
                        indication = null,
                        role = Role.RadioButton,
                        onClick = { onSelect(index) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (on) Lime else Titanium,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                )
            }
        }
    }
}

/** Label above a control, used to keep settings groups tidy. */
@Composable
fun Field(label: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier.fillMaxWidth(), verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.titleSmall, color = Titanium)
        content()
    }
}

/** Single-line input in the deck style (no Material text field). */
@Composable
fun DeckTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier
) {
    val shape = ChamferShape(10.dp, topLeft = true)
    val style = MaterialTheme.typography.labelSmall.copy(fontSize = 14.sp, color = TextPrimary)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = style,
        cursorBrush = SolidColor(Lime),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(shape)
            .background(Graphite0)
            .border(1.dp, Graphite3, shape)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        decorationBox = { inner ->
            Box {
                if (value.isEmpty()) Text(hint, style = style.copy(color = TextMuted), maxLines = 1)
                inner()
            }
        }
    )
}
