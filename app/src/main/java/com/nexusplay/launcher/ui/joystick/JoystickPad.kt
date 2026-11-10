package com.nexusplay.launcher.ui.joystick

import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import android.view.HapticFeedbackConstants
import com.nexusplay.launcher.input.JoystickMath
import com.nexusplay.launcher.input.JoystickSettings
import com.nexusplay.launcher.input.ResponseMode
import com.nexusplay.launcher.input.ReturnMode
import com.nexusplay.launcher.input.StickMode
import com.nexusplay.launcher.input.StickOutput
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.min

/**
 * Interactive stick. It owns one pointer from touch-down to lift and ignores every other finger,
 * so other controls can be used at the same time. Settings are read on every event, which is
 * why changing a slider while holding the stick takes effect immediately.
 */
@Composable
fun JoystickPad(
    settings: JoystickSettings,
    modifier: Modifier = Modifier,
    showDeadzone: Boolean = true,
    onOutput: (StickOutput) -> Unit,
    onRelease: (StickOutput) -> Unit = {}
) {
    val view = LocalView.current
    val dens = LocalDensity.current
    val current by rememberUpdatedState(settings)
    val emit by rememberUpdatedState(onOutput)
    val released by rememberUpdatedState(onRelease)

    var knob by remember { mutableStateOf(Offset.Zero) }
    var baseCenter by remember { mutableStateOf(Offset.Zero) }
    var fingerDown by remember { mutableStateOf(false) }
    var engaged by remember { mutableStateOf(false) }
    var out by remember { mutableStateOf(StickOutput.Zero) }

    Box(
        modifier.pointerInput(Unit) {
            coroutineScope {
                val scope = this
                var animJob: Job? = null

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val w = size.width.toFloat()
                    val h = size.height.toFloat()
                    val s0 = current
                    val baseR0 = min(s0.sizeDp * dens.density, min(w, h)) / 2f
                    val zoneCenter = Offset(w / 2f, h / 2f)
                    val center: Offset
                    if (s0.mode == StickMode.Fixed) {
                        // Fixed sticks only react to touches on (or just around) the base.
                        if ((down.position - zoneCenter).getDistance() > baseR0 * 1.25f) return@awaitEachGesture
                        center = zoneCenter
                    } else {
                        center = Offset(
                            down.position.x.coerceIn(baseR0, w - baseR0),
                            down.position.y.coerceIn(baseR0, h - baseR0)
                        )
                    }

                    down.consume()
                    animJob?.cancel()
                    baseCenter = center
                    fingerDown = true
                    engaged = true
                    if (s0.haptics) view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    var atEdge = false

                    fun travelOf(s: JoystickSettings): Float =
                        (min(s.sizeDp * dens.density, min(w, h)) / 2f * s.travel).coerceAtLeast(1f)

                    fun publish(k: Offset) {
                        val s = current
                        val tr = travelOf(s)
                        val o = JoystickMath.compute(k.x / tr, k.y / tr, s)
                        out = o
                        emit(o)
                        if (s.haptics) {
                            if (!atEdge && o.rawMagnitude >= 0.98f) {
                                atEdge = true
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            } else if (atEdge && o.rawMagnitude < 0.9f) {
                                atEdge = false
                            }
                        }
                    }

                    fun follow(pos: Offset) {
                        val s = current
                        val tr = travelOf(s)
                        val d = pos - center
                        val len = d.getDistance()
                        val target = if (len > tr && len > 0f) d * (tr / len) else d
                        animJob?.cancel()
                        if (s.response == ResponseMode.Smooth) {
                            val from = knob
                            animJob = scope.launch {
                                animate(
                                    Offset.VectorConverter, from, target,
                                    animationSpec = spring(dampingRatio = 1f, stiffness = 700f)
                                ) { v, _ ->
                                    knob = v
                                    publish(v)
                                }
                            }
                        } else {
                            knob = target
                            publish(target)
                        }
                    }

                    try {
                        follow(down.position)
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break
                            change.consume()
                            follow(change.position)
                        }
                    } catch (e: CancellationException) {
                        animJob?.cancel()
                        knob = Offset.Zero
                        out = StickOutput.Zero
                        emit(StickOutput.Zero)
                        fingerDown = false
                        engaged = false
                        throw e
                    }

                    fingerDown = false
                    animJob?.cancel()
                    val s = current
                    when (s.returnMode) {
                        ReturnMode.Snap -> {
                            knob = Offset.Zero
                            out = StickOutput.Zero
                            emit(StickOutput.Zero)
                            engaged = false
                            released(StickOutput.Zero)
                        }
                        ReturnMode.Hold -> released(out)
                        ReturnMode.Spring, ReturnMode.Soft -> {
                            val from = knob
                            val damping = if (s.returnMode == ReturnMode.Spring) 0.55f else 1f
                            animJob = scope.launch {
                                animate(
                                    Offset.VectorConverter, from, Offset.Zero,
                                    animationSpec = spring(dampingRatio = damping, stiffness = 450f)
                                ) { v, _ ->
                                    knob = v
                                    publish(v)
                                }
                                knob = Offset.Zero
                                out = StickOutput.Zero
                                emit(StickOutput.Zero)
                                engaged = false
                                released(StickOutput.Zero)
                            }
                        }
                    }
                }
            }
        }
    ) {
        Canvas(
            Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = current.opacity }
        ) {
            val s = current
            val baseR = min(s.sizeDp * density, min(size.width, size.height)) / 2f
            val idle = Offset(size.width / 2f, size.height / 2f)
            val atTouch = s.mode == StickMode.Floating && engaged
            drawStick(
                center = if (atTouch) baseCenter else idle,
                baseR = baseR,
                knob = knob,
                travelPx = baseR * s.travel,
                s = s,
                pressed = fingerDown,
                out = out,
                showDeadzone = showDeadzone,
                ghost = if (s.mode == StickMode.Floating && !engaged) 0.4f else 1f
            )
        }
    }
}
