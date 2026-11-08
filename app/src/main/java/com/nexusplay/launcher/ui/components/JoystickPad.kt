package com.nexusplay.launcher.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import com.nexusplay.launcher.input.JoystickConfig
import com.nexusplay.launcher.input.JoystickOutput
import com.nexusplay.launcher.input.JoystickProcessor
import com.nexusplay.launcher.ui.theme.AccentCyan
import com.nexusplay.launcher.ui.theme.AccentLime
import com.nexusplay.launcher.ui.theme.AccentOrange
import com.nexusplay.launcher.ui.theme.Graphite2
import com.nexusplay.launcher.ui.theme.Hairline
import com.nexusplay.launcher.ui.theme.Titanium
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.channels.Channel

/**
 * Touch surface plus renderer for one analog stick. It fills the zone it is given; the stick sits at
 * (posX, posY) of that zone in fixed mode and appears under the thumb in floating mode.
 *
 * Only the pointer that first lands on the stick is followed (by pointer id), every change it consumes
 * is its own, so other controls in sibling zones keep receiving their own fingers. The pointer is always
 * released in a finally block, so cancellation or leaving the screen can never leave the stick held.
 * Frames are only scheduled while the stick is moving; an idle stick costs nothing.
 */
@Composable
fun JoystickPad(
    config: JoystickConfig,
    modifier: Modifier = Modifier,
    showGuides: Boolean = false,
    onOutput: (JoystickOutput) -> Unit = {}
) {
    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current
    val processor = remember { JoystickProcessor() }
    var output by remember { mutableStateOf(JoystickOutput()) }
    val wake = remember { Channel<Unit>(Channel.CONFLATED) }
    val currentConfig by rememberUpdatedState(config)
    val currentOnOutput by rememberUpdatedState(onOutput)
    val radiusPx = with(density) { (config.sizeDp / 2f).dp.toPx() }

    SideEffect {
        processor.config = config
        processor.radiusPx = radiusPx
    }

    // Disabling the stick, or switching away from hold-position mode, drops any held deflection.
    LaunchedEffect(config.enabled, config.returnToCenter) {
        if (!processor.touching) {
            processor.reset()
            output = JoystickOutput()
            currentOnOutput(output)
        }
    }

    LaunchedEffect(Unit) {
        for (ignored in wake) {
            while (processor.needsFrame()) {
                androidx.compose.runtime.withFrameNanos { nanos ->
                    val o = processor.update(nanos / 1_000_000L)
                    output = o
                    currentOnOutput(o)
                    if (processor.takeHaptic()) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            }
        }
    }

    Canvas(
        modifier
            .fillMaxSize()
            .alpha(config.opacity)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = true)
                    val cfg = currentConfig
                    val cx = size.width * cfg.posX
                    val cy = size.height * cfg.posY
                    if (!processor.down(down.position.x, down.position.y, down.uptimeMillis, cx, cy)) return@awaitEachGesture
                    down.consume()
                    wake.trySend(Unit)
                    val id = down.id
                    var lastMs = down.uptimeMillis
                    try {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == id } ?: break
                            lastMs = change.uptimeMillis
                            if (!change.pressed) { change.consume(); break }
                            processor.move(change.position.x, change.position.y)
                            change.consume()
                            wake.trySend(Unit)
                        }
                    } finally {
                        processor.up(lastMs)
                        wake.trySend(Unit)
                    }
                }
            }
    ) {
        val cfg = config
        val r = radiusPx
        val travel = r * cfg.range
        val o = output
        val held = o.active || o.thumbX != 0f || o.thumbY != 0f
        val base = if (held) Offset(processor.baseX, processor.baseY)
        else Offset(size.width * cfg.posX, size.height * cfg.posY)
        val ghost = cfg.floating && !held
        val ink = if (o.active) AccentLime else Titanium
        val thin = 1.5.dp.toPx()
        val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))

        if (showGuides) {
            drawLine(Hairline, Offset(base.x - travel * 1.3f, base.y), Offset(base.x + travel * 1.3f, base.y), 1f)
            drawLine(Hairline, Offset(base.x, base.y - travel * 1.3f), Offset(base.x, base.y + travel * 1.3f), 1f)
            drawCircle(AccentOrange.copy(alpha = 0.16f), cfg.innerDeadzone * travel, base)
            drawCircle(AccentOrange.copy(alpha = 0.7f), cfg.innerDeadzone * travel, base, style = Stroke(thin))
            val sat = (1f - cfg.outerDeadzone).coerceAtLeast(cfg.innerDeadzone + 0.1f) * travel
            drawCircle(AccentCyan.copy(alpha = 0.6f), sat, base, style = Stroke(thin, pathEffect = dash))
        }

        drawCircle(
            ink.copy(alpha = if (ghost) 0.35f else 0.6f), r, base,
            style = Stroke(2.dp.toPx(), pathEffect = if (ghost) dash else null)
        )
        if (!ghost) {
            drawCircle(Hairline, r * 0.62f, base, style = Stroke(thin))
            for (i in 0 until 24) {
                val a = Math.toRadians(i * 15.0)
                val len = if (i % 6 == 0) 10.dp.toPx() else 5.dp.toPx()
                val c = cos(a).toFloat(); val s = sin(a).toFloat()
                drawLine(
                    Titanium.copy(alpha = if (i % 6 == 0) 0.8f else 0.35f),
                    Offset(base.x + c * (r + 4.dp.toPx()), base.y + s * (r + 4.dp.toPx())),
                    Offset(base.x + c * (r + 4.dp.toPx() + len), base.y + s * (r + 4.dp.toPx() + len)),
                    1.5f
                )
            }
            val thumb = Offset(base.x + o.thumbX * travel, base.y + o.thumbY * travel)
            drawLine(ink.copy(alpha = 0.5f), base, thumb, 3.dp.toPx())
            drawCircle(Graphite2, r * 0.38f, thumb)
            drawCircle(ink, r * 0.38f, thumb, style = Stroke(2.dp.toPx()))
            drawCircle(AccentLime, r * 0.07f, thumb)
        }
    }
}
