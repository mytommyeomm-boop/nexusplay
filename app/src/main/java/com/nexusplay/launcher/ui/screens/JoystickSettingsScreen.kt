package com.nexusplay.launcher.ui.screens

import androidx.compose.ui.res.stringResource
import com.nexusplay.launcher.core.text
import com.nexusplay.launcher.core.UiText
import com.nexusplay.launcher.R
import android.os.Vibrator
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexusplay.launcher.input.AxisId
import com.nexusplay.launcher.input.CapDesign
import com.nexusplay.launcher.input.ControllerOutputHub
import com.nexusplay.launcher.input.JoystickLimits
import com.nexusplay.launcher.input.JoystickMath
import com.nexusplay.launcher.input.JoystickSettings
import com.nexusplay.launcher.input.LocalJoystickSettings
import com.nexusplay.launcher.input.OutputStatus
import com.nexusplay.launcher.input.ResponseCurve
import com.nexusplay.launcher.input.ResponseMode
import com.nexusplay.launcher.input.ReturnMode
import com.nexusplay.launcher.input.RingDesign
import com.nexusplay.launcher.input.StickMode
import com.nexusplay.launcher.input.StickOutput
import com.nexusplay.launcher.input.StickSide
import com.nexusplay.launcher.input.StickStyle
import com.nexusplay.launcher.ui.components.ChamferShape
import com.nexusplay.launcher.ui.components.DeckPanel
import com.nexusplay.launcher.ui.components.DeckSegmented
import com.nexusplay.launcher.ui.components.DeckSlider
import com.nexusplay.launcher.ui.components.Field
import com.nexusplay.launcher.ui.components.KeyButton
import com.nexusplay.launcher.ui.components.SectionTitle
import com.nexusplay.launcher.ui.components.SliderRow
import com.nexusplay.launcher.ui.components.StatusLed
import com.nexusplay.launcher.ui.components.ToggleRow
import com.nexusplay.launcher.ui.joystick.JoystickPad
import com.nexusplay.launcher.ui.joystick.accentOf
import com.nexusplay.launcher.ui.joystick.drawStick
import com.nexusplay.launcher.ui.theme.Cyan
import com.nexusplay.launcher.ui.theme.Graphite0
import com.nexusplay.launcher.ui.theme.Graphite3
import com.nexusplay.launcher.ui.theme.Lime
import com.nexusplay.launcher.ui.theme.Orange
import com.nexusplay.launcher.ui.theme.TextMuted
import com.nexusplay.launcher.ui.theme.Titanium
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

private sealed interface CalibState {
    data object Idle : CalibState
    data class Running(val secondsLeft: Int) : CalibState
    data class Done(val gain: Float) : CalibState
    data class Failed(val message: UiText) : CalibState
}

private val DIRECTION_RES = intArrayOf(
    R.string.dir_right, R.string.dir_up_right, R.string.dir_up, R.string.dir_up_left,
    R.string.dir_left, R.string.dir_down_left, R.string.dir_down, R.string.dir_down_right
)

private fun f2(v: Float) = String.format(Locale.US, "%.2f", v)
private fun signed(v: Float) = String.format(Locale.US, "%+.2f", v)
private fun pct(v: Float) = "${(v * 100f).roundToInt()}%"

@Composable
fun JoystickSettingsScreen(contentPadding: PaddingValues, onBack: () -> Unit) {
    val holder = LocalJoystickSettings.current
    val s = holder.settings
    val context = LocalContext.current
    val sessionStart = remember { holder.settings }
    val hasVibrator = remember { context.getSystemService(Vibrator::class.java)?.hasVibrator() == true }

    var out by remember { mutableStateOf(StickOutput.Zero) }
    var peak by remember { mutableFloatStateOf(0f) }
    var centering by remember { mutableStateOf(UiText(R.string.js_status_not_tested)) }
    var calib by remember { mutableStateOf<CalibState>(CalibState.Idle) }
    var calibPeak by remember { mutableFloatStateOf(0f) }
    var confirmReset by remember { mutableStateOf(false) }

    // Calibration: 4 seconds of full-circle movement; the farthest reach becomes the new "full".
    LaunchedEffect(calib is CalibState.Running) {
        if (calib is CalibState.Running) {
            for (left in 4 downTo 1) {
                calib = CalibState.Running(left)
                delay(1000)
            }
            val reach = calibPeak
            calib = if (reach < 0.5f) {
                CalibState.Failed(UiText(R.string.js_calib_failed, pct(reach)))
            } else {
                val gain = (1f / reach).coerceIn(JoystickLimits.Gain.start, JoystickLimits.Gain.endInclusive)
                holder.update { it.copy(calibrationGain = gain) }
                CalibState.Done(gain)
            }
        }
    }
    LaunchedEffect(confirmReset) {
        if (confirmReset) {
            delay(3000)
            confirmReset = false
        }
    }

    val axis = if (s.side == StickSide.Left) AxisId.LeftStick else AxisId.RightStick
    val bridge = ControllerOutputHub.active

    Column(
        Modifier
            .fillMaxSize()
            .padding(top = contentPadding.calculateTopPadding() + 8.dp)
    ) {
        TopBar(saveFailed = holder.lastSaveFailed, onBack = onBack)
        Spacer(Modifier.height(12.dp))
        TestPlate(
            s = s,
            out = out,
            peak = peak,
            status = when (val c = calib) {
                is CalibState.Running -> UiText(R.string.js_calib_running, c.secondsLeft)
                is CalibState.Done -> UiText(R.string.js_calib_done, pct(1f / c.gain))
                is CalibState.Failed -> c.message
                CalibState.Idle -> centering
            },
            statusWarn = calib is CalibState.Failed,
            calibrating = calib is CalibState.Running,
            onOutput = { o ->
                out = o
                if (o.magnitude > peak) peak = o.magnitude
                if (calib is CalibState.Running) calibPeak = max(calibPeak, o.rawMagnitude)
                bridge.setAxis(axis, o.x, o.y)
            },
            onRelease = { o -> centering = describeCentering(o, s.returnMode) },
            onCalibrate = { calibPeak = 0f; calib = CalibState.Running(4) },
            onResetCalibration = {
                holder.update { it.copy(calibrationGain = 1f) }
                calib = CalibState.Idle
            },
            onResetPeak = { peak = 0f }
        )
        Text(
            when (val st = bridge.status) {
                OutputStatus.Ready -> UiText(R.string.js_bridge_ready, bridge.name)
                is OutputStatus.Unavailable -> UiText(R.string.js_bridge_not_connected, st.reason)
            }.text(),
            style = MaterialTheme.typography.bodySmall,
            color = if (bridge.status is OutputStatus.Ready) Lime else TextMuted,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(top = 18.dp, bottom = contentPadding.calculateBottomPadding() + 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Group(stringResource(R.string.group_sensitivity), ChamferShape(16.dp, topLeft = true)) {
                    SliderRow(stringResource(R.string.js_sens_overall), "${f2(s.sensitivity)}x", s.sensitivity, JoystickLimits.Sensitivity,
                        { v -> holder.update { it.copy(sensitivity = v) } }, step = 0.01f)
                    SliderRow(stringResource(R.string.js_sens_x), "${f2(s.sensitivityX)}x", s.sensitivityX, JoystickLimits.Sensitivity,
                        { v -> holder.update { it.copy(sensitivityX = v) } }, step = 0.01f)
                    SliderRow(stringResource(R.string.js_sens_y), "${f2(s.sensitivityY)}x", s.sensitivityY, JoystickLimits.Sensitivity,
                        { v -> holder.update { it.copy(sensitivityY = v) } }, step = 0.01f)
                }
            }
            item {
                Group(stringResource(R.string.group_deadzone), ChamferShape(16.dp, topRight = true)) {
                    SliderRow(stringResource(R.string.js_deadzone_inner), pct(s.deadzone), s.deadzone, JoystickLimits.Deadzone,
                        { v -> holder.update { it.copy(deadzone = v) } }, step = 0.01f)
                    Text(
                        stringResource(R.string.js_deadzone_note),
                        style = MaterialTheme.typography.bodySmall, color = Titanium
                    )
                }
            }
            item {
                Group(stringResource(R.string.group_size_position), ChamferShape(16.dp, bottomRight = true)) {
                    SliderRow(stringResource(R.string.js_size), "${s.sizeDp.roundToInt()} dp", s.sizeDp, JoystickLimits.Size,
                        { v -> holder.update { it.copy(sizeDp = v) } }, step = 1f)
                    SliderRow(stringResource(R.string.js_travel), pct(s.travel), s.travel, JoystickLimits.Travel,
                        { v -> holder.update { it.copy(travel = v) } }, step = 0.01f)
                    Field(stringResource(R.string.js_mode)) {
                        DeckSegmented(listOf(stringResource(R.string.js_mode_fixed), stringResource(R.string.js_mode_floating)), s.mode.ordinal) { i ->
                            holder.update { it.copy(mode = StickMode.entries[i]) }
                        }
                    }
                    Field(stringResource(R.string.js_side)) {
                        DeckSegmented(listOf(stringResource(R.string.js_side_left), stringResource(R.string.js_side_right)), s.side.ordinal) { i ->
                            holder.update { it.copy(side = StickSide.entries[i], positionX = if (i == 0) 0.18f else 0.82f) }
                        }
                    }
                    Field(stringResource(R.string.js_position)) {
                        PositionPad(s) { x, y -> holder.update { it.copy(positionX = x, positionY = y) } }
                    }
                    Text(
                        stringResource(R.string.js_position_note),
                        style = MaterialTheme.typography.bodySmall, color = Titanium
                    )
                }
            }
            item {
                Group(stringResource(R.string.group_movement), ChamferShape(16.dp, bottomLeft = true)) {
                    Field(stringResource(R.string.js_response)) {
                        DeckSegmented(listOf(stringResource(R.string.js_response_responsive), stringResource(R.string.js_response_smooth)), s.response.ordinal) { i ->
                            holder.update { it.copy(response = ResponseMode.entries[i]) }
                        }
                    }
                    Field(stringResource(R.string.js_curve)) {
                        DeckSegmented(listOf(stringResource(R.string.js_curve_linear), stringResource(R.string.js_curve_exponential)), s.curve.ordinal) { i ->
                            holder.update { it.copy(curve = ResponseCurve.entries[i]) }
                        }
                    }
                    if (s.curve == ResponseCurve.Exponential) {
                        SliderRow(stringResource(R.string.js_curve_strength), f2(s.curveExponent), s.curveExponent, JoystickLimits.Exponent,
                            { v -> holder.update { it.copy(curveExponent = v) } }, step = 0.05f)
                    }
                    CurvePlot(s)
                    ToggleRow(stringResource(R.string.js_invert_x), s.invertX, { v -> holder.update { it.copy(invertX = v) } })
                    ToggleRow(stringResource(R.string.js_invert_y), s.invertY, { v -> holder.update { it.copy(invertY = v) } })
                    Field(stringResource(R.string.js_return)) {
                        DeckSegmented(listOf(stringResource(R.string.js_return_spring), stringResource(R.string.js_return_soft), stringResource(R.string.js_return_snap), stringResource(R.string.js_return_hold)), s.returnMode.ordinal) { i ->
                            holder.update { it.copy(returnMode = ReturnMode.entries[i]) }
                        }
                    }
                }
            }
            item {
                Group(stringResource(R.string.group_appearance), ChamferShape(16.dp, topLeft = true)) {
                    Field(stringResource(R.string.js_style)) {
                        DeckSegmented(listOf(stringResource(R.string.js_style_metallic), stringResource(R.string.js_style_minimal), stringResource(R.string.js_style_futuristic)), s.style.ordinal) { i ->
                            holder.update { it.copy(style = StickStyle.entries[i]) }
                        }
                    }
                    Field(stringResource(R.string.js_ring)) {
                        DeckSegmented(listOf(stringResource(R.string.js_ring_plain), stringResource(R.string.js_ring_ticked), stringResource(R.string.js_ring_segmented)), s.ring.ordinal) { i ->
                            holder.update { it.copy(ring = RingDesign.entries[i]) }
                        }
                    }
                    Field(stringResource(R.string.js_cap)) {
                        DeckSegmented(listOf(stringResource(R.string.js_cap_dome), stringResource(R.string.js_cap_flat), stringResource(R.string.js_cap_ringed)), s.cap.ordinal) { i ->
                            holder.update { it.copy(cap = CapDesign.entries[i]) }
                        }
                    }
                    SliderRow(stringResource(R.string.js_opacity), pct(s.opacity), s.opacity, JoystickLimits.Opacity,
                        { v -> holder.update { it.copy(opacity = v) } }, step = 0.01f)
                    SliderRow(stringResource(R.string.js_lighting), pct(s.lighting), s.lighting, JoystickLimits.Lighting,
                        { v -> holder.update { it.copy(lighting = v) } }, step = 0.01f)
                    Field(stringResource(R.string.js_accent)) {
                        Swatches(s.accentHue) { h -> holder.update { it.copy(accentHue = h) } }
                        DeckSlider(s.accentHue, { h -> holder.update { it.copy(accentHue = h) } }, JoystickLimits.Hue)
                    }
                }
            }
            item {
                Group(stringResource(R.string.group_feedback), ChamferShape(16.dp, topRight = true)) {
                    ToggleRow(
                        stringResource(R.string.js_haptics), s.haptics && hasVibrator, { v -> holder.update { it.copy(haptics = v) } },
                        caption = stringResource(if (hasVibrator) R.string.js_haptics_caption else R.string.js_haptics_none),
                        enabled = hasVibrator
                    )
                    ToggleRow(
                        stringResource(R.string.js_touch_feedback), s.touchFeedback, { v -> holder.update { it.copy(touchFeedback = v) } },
                        caption = stringResource(R.string.js_touch_caption)
                    )
                    Text(
                        stringResource(R.string.js_gain_note, f2(s.calibrationGain)),
                        style = MaterialTheme.typography.bodySmall, color = Titanium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        KeyButton(
                            stringResource(R.string.js_restore_previous), { holder.replace(sessionStart) },
                            modifier = Modifier.weight(1f), enabled = s != sessionStart
                        )
                        KeyButton(
                            stringResource(if (confirmReset) R.string.js_reset_confirm else R.string.js_reset_default),
                            {
                                if (confirmReset) {
                                    holder.replace(JoystickSettings())
                                    calib = CalibState.Idle
                                    confirmReset = false
                                } else confirmReset = true
                            },
                            modifier = Modifier.weight(1f), highlight = confirmReset
                        )
                    }
                }
            }
        }
    }
}

private fun describeCentering(o: StickOutput, mode: ReturnMode): UiText = when {
    mode == ReturnMode.Hold -> UiText(R.string.js_status_held, signed(o.x), signed(o.y))
    abs(o.x) < 0.001f && abs(o.y) < 0.001f -> UiText(R.string.js_status_centered)
    else -> UiText(R.string.js_status_off_center, signed(o.x), signed(o.y))
}

@Composable
private fun TopBar(saveFailed: Boolean, onBack: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KeyButton(stringResource(R.string.action_back), onBack)
        Spacer(Modifier.width(14.dp))
        Text(stringResource(R.string.js_title), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
        StatusLed(if (saveFailed) Orange else Lime)
        Spacer(Modifier.width(8.dp))
        Text(
            stringResource(if (saveFailed) R.string.js_save_failed else R.string.js_saved),
            style = MaterialTheme.typography.labelSmall,
            color = if (saveFailed) Orange else Cyan
        )
    }
}

@Composable
private fun TestPlate(
    s: JoystickSettings,
    out: StickOutput,
    peak: Float,
    status: UiText,
    statusWarn: Boolean,
    calibrating: Boolean,
    onOutput: (StickOutput) -> Unit,
    onRelease: (StickOutput) -> Unit,
    onCalibrate: () -> Unit,
    onResetCalibration: () -> Unit,
    onResetPeak: () -> Unit
) {
    DeckPanel(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = ChamferShape(22.dp, topLeft = true, bottomRight = true),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(Modifier.fillMaxWidth().height(200.dp)) {
            JoystickPad(s, Modifier.fillMaxSize(), onOutput = onOutput, onRelease = onRelease)
            Column(Modifier.align(Alignment.TopStart).padding(start = 16.dp, top = 14.dp)) {
                val mono = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp)
                Text("X ${signed(out.x)}", style = mono, color = Cyan)
                Text("Y ${signed(out.y)}", style = mono, color = Cyan)
                Text(
                    if (out.active) "${out.angleDeg.roundToInt()}\u00B0 ${stringResource(DIRECTION_RES[JoystickMath.compassIndex(out.angleDeg)])}" else stringResource(R.string.js_idle),
                    style = mono, color = Titanium
                )
            }
            AnalogPlot(
                s, out,
                Modifier.align(Alignment.TopEnd).padding(top = 12.dp, end = 14.dp).size(72.dp)
            )
        }
        Column(Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusLed(if (statusWarn) Orange else Lime, pulse = calibrating)
                Spacer(Modifier.width(8.dp))
                Text(
                    status.text(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (statusWarn) Orange else Titanium,
                    maxLines = 2,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KeyButton(stringResource(R.string.js_key_peak, f2(peak)), onResetPeak, modifier = Modifier.weight(1f))
                KeyButton(stringResource(R.string.js_key_reset_cal), onResetCalibration, modifier = Modifier.weight(1f), enabled = s.calibrationGain != 1f)
                KeyButton(stringResource(R.string.js_key_calibrate), onCalibrate, modifier = Modifier.weight(1f), enabled = !calibrating, highlight = calibrating)
            }
        }
    }
}

/** Square = output range, circle = stick travel. Dashed ring = deadzone, hollow dot = finger, solid dot = output. */
@Composable
private fun AnalogPlot(s: JoystickSettings, out: StickOutput, modifier: Modifier) {
    val accent = accentOf(s)
    val shape = ChamferShape(8.dp, topLeft = true)
    Canvas(
        modifier
            .clip(shape)
            .background(Graphite0.copy(alpha = 0.8f))
            .border(1.dp, Graphite3, shape)
    ) {
        val r = size.minDimension / 2f - 5.dp.toPx()
        val c = Offset(size.width / 2f, size.height / 2f)
        val grid = Titanium.copy(alpha = 0.3f)
        drawRect(grid, c - Offset(r, r), Size(r * 2f, r * 2f), style = Stroke(1f))
        drawCircle(grid, r, c, style = Stroke(1f))
        drawLine(grid, Offset(c.x - r, c.y), Offset(c.x + r, c.y), 1f)
        drawLine(grid, Offset(c.x, c.y - r), Offset(c.x, c.y + r), 1f)
        drawCircle(
            accent.copy(alpha = 0.7f), s.deadzone / s.calibrationGain * r, c,
            style = Stroke(1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 3.dp.toPx())))
        )
        drawCircle(Titanium, 3.dp.toPx(), c + Offset(out.inX * r, -out.inY * r), style = Stroke(1.dp.toPx()))
        drawCircle(accent, 4.dp.toPx(), c + Offset(out.x * r, -out.y * r))
    }
}

/** Input-to-output curve for the current deadzone, curve, sensitivity and calibration. */
@Composable
private fun CurvePlot(s: JoystickSettings) {
    val accent = accentOf(s)
    val shape = ChamferShape(10.dp, topLeft = true)
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(84.dp)
            .clip(shape)
            .background(Graphite0)
            .border(1.dp, Graphite3, shape)
    ) {
        val pad = 10.dp.toPx()
        val w = size.width - 2 * pad
        val h = size.height - 2 * pad
        val axis = Titanium.copy(alpha = 0.3f)
        drawLine(axis, Offset(pad, pad + h), Offset(pad + w, pad + h), 1f)
        drawLine(axis, Offset(pad, pad), Offset(pad, pad + h), 1f)
        drawLine(axis, Offset(pad, pad + h), Offset(pad + w, pad), 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f)))
        val st = s.copy(invertX = false, invertY = false)
        val path = Path()
        for (i in 0..48) {
            val m = i / 48f
            val o = JoystickMath.compute(m, 0f, st).x
            val x = pad + w * m
            val y = pad + h * (1f - o)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, accent, style = Stroke(2.dp.toPx(), join = StrokeJoin.Round))
        val dz = pad + w * (s.deadzone / s.calibrationGain).coerceIn(0f, 1f)
        drawLine(accent.copy(alpha = 0.5f), Offset(dz, pad), Offset(dz, pad + h), 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 4f)))
    }
}

/** Miniature landscape screen: drag the stick to choose where Gaming Mode will place it. */
@Composable
private fun PositionPad(s: JoystickSettings, onMove: (Float, Float) -> Unit) {
    val shape = ChamferShape(12.dp, topRight = true)
    val latest by rememberUpdatedState(onMove)
    Canvas(
        Modifier
            .fillMaxWidth()
            .aspectRatio(2.1f)
            .clip(shape)
            .background(Graphite0)
            .border(1.dp, Graphite3, shape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { p -> latest(p.x / size.width, p.y / size.height) },
                    onDrag = { change, _ ->
                        change.consume()
                        latest(change.position.x / size.width, change.position.y / size.height)
                    }
                )
            }
    ) {
        val grid = Titanium.copy(alpha = 0.12f)
        for (i in 1..7) drawLine(grid, Offset(size.width * i / 8f, 0f), Offset(size.width * i / 8f, size.height), 1f)
        for (i in 1..3) drawLine(grid, Offset(0f, size.height * i / 4f), Offset(size.width, size.height * i / 4f), 1f)
        // A typical landscape phone is about 800dp wide; scale the stick to the same proportion.
        val scale = size.width / (800f * density)
        val baseR = s.sizeDp * density * scale / 2f
        drawStick(
            center = Offset(s.positionX * size.width, s.positionY * size.height),
            baseR = baseR, knob = Offset.Zero, travelPx = baseR * s.travel,
            s = s, pressed = false, out = null, showDeadzone = false
        )
    }
}

@Composable
private fun Swatches(hue: Float, onPick: (Float) -> Unit) {
    val presets = listOf(72f, 187f, 24f, 2f)
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        presets.forEach { h ->
            val on = abs(hue - h) < 1f
            val shape = ChamferShape(8.dp, topLeft = true, bottomRight = true)
            val interaction = remember { MutableInteractionSource() }
            Box(
                Modifier
                    .size(44.dp)
                    .clip(shape)
                    .background(Color.hsv(h, 0.85f, 1f))
                    .border(if (on) 2.dp else 1.dp, if (on) Color.White else Graphite3, shape)
                    .selectable(
                        selected = on, interactionSource = interaction, indication = null,
                        role = Role.RadioButton, onClick = { onPick(h) }
                    )
            )
        }
    }
}

@Composable
private fun Group(title: String, shape: ChamferShape, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle(title)
        DeckPanel(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = shape,
            contentPadding = PaddingValues(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp), content = content)
        }
    }
}
