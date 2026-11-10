package com.nexusplay.launcher.ui.screens

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
    data class Failed(val message: String) : CalibState
}

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
    var centering by remember { mutableStateOf("Centering not tested yet") }
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
                CalibState.Failed("Reach was only ${pct(reach)}. Push closer to the edge and retry.")
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
                is CalibState.Running -> "Rotate the stick around its edge: ${c.secondsLeft}s"
                is CalibState.Done -> "Calibrated: full output at ${pct(1f / c.gain)} reach"
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
                OutputStatus.Ready -> "Game output: ${bridge.name} connected"
                is OutputStatus.Unavailable -> "Game output: not connected. ${st.reason}"
            },
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
                Group("Sensitivity", ChamferShape(16.dp, topLeft = true)) {
                    SliderRow("Overall", "${f2(s.sensitivity)}x", s.sensitivity, JoystickLimits.Sensitivity,
                        { v -> holder.update { it.copy(sensitivity = v) } }, step = 0.01f)
                    SliderRow("X axis", "${f2(s.sensitivityX)}x", s.sensitivityX, JoystickLimits.Sensitivity,
                        { v -> holder.update { it.copy(sensitivityX = v) } }, step = 0.01f)
                    SliderRow("Y axis", "${f2(s.sensitivityY)}x", s.sensitivityY, JoystickLimits.Sensitivity,
                        { v -> holder.update { it.copy(sensitivityY = v) } }, step = 0.01f)
                }
            }
            item {
                Group("Deadzone", ChamferShape(16.dp, topRight = true)) {
                    SliderRow("Inner deadzone", pct(s.deadzone), s.deadzone, JoystickLimits.Deadzone,
                        { v -> holder.update { it.copy(deadzone = v) } }, step = 0.01f)
                    Text(
                        "Movement inside the dashed circle produces no output, which filters out resting-thumb jitter and accidental touches.",
                        style = MaterialTheme.typography.bodySmall, color = Titanium
                    )
                }
            }
            item {
                Group("Size and position", ChamferShape(16.dp, bottomRight = true)) {
                    SliderRow("Size", "${s.sizeDp.roundToInt()} dp", s.sizeDp, JoystickLimits.Size,
                        { v -> holder.update { it.copy(sizeDp = v) } }, step = 1f)
                    SliderRow("Movement radius", pct(s.travel), s.travel, JoystickLimits.Travel,
                        { v -> holder.update { it.copy(travel = v) } }, step = 0.01f)
                    Field("Mode") {
                        DeckSegmented(listOf("Fixed", "Floating"), s.mode.ordinal) { i ->
                            holder.update { it.copy(mode = StickMode.entries[i]) }
                        }
                    }
                    Field("Side") {
                        DeckSegmented(listOf("Left", "Right"), s.side.ordinal) { i ->
                            holder.update { it.copy(side = StickSide.entries[i], positionX = if (i == 0) 0.18f else 0.82f) }
                        }
                    }
                    Field("Position on the gaming screen") {
                        PositionPad(s) { x, y -> holder.update { it.copy(positionX = x, positionY = y) } }
                    }
                    Text(
                        "Drag the stick to place it. The position is saved now and is applied by Gaming Mode in the next stage.",
                        style = MaterialTheme.typography.bodySmall, color = Titanium
                    )
                }
            }
            item {
                Group("Movement", ChamferShape(16.dp, bottomLeft = true)) {
                    Field("Response") {
                        DeckSegmented(listOf("Responsive", "Smooth"), s.response.ordinal) { i ->
                            holder.update { it.copy(response = ResponseMode.entries[i]) }
                        }
                    }
                    Field("Response curve") {
                        DeckSegmented(listOf("Linear", "Exponential"), s.curve.ordinal) { i ->
                            holder.update { it.copy(curve = ResponseCurve.entries[i]) }
                        }
                    }
                    if (s.curve == ResponseCurve.Exponential) {
                        SliderRow("Curve strength", f2(s.curveExponent), s.curveExponent, JoystickLimits.Exponent,
                            { v -> holder.update { it.copy(curveExponent = v) } }, step = 0.05f)
                    }
                    CurvePlot(s)
                    ToggleRow("Invert X axis", s.invertX, { v -> holder.update { it.copy(invertX = v) } })
                    ToggleRow("Invert Y axis", s.invertY, { v -> holder.update { it.copy(invertY = v) } })
                    Field("When released") {
                        DeckSegmented(listOf("Spring", "Soft", "Snap", "Hold"), s.returnMode.ordinal) { i ->
                            holder.update { it.copy(returnMode = ReturnMode.entries[i]) }
                        }
                    }
                }
            }
            item {
                Group("Appearance", ChamferShape(16.dp, topLeft = true)) {
                    Field("Style") {
                        DeckSegmented(listOf("Metallic", "Minimal", "Futuristic"), s.style.ordinal) { i ->
                            holder.update { it.copy(style = StickStyle.entries[i]) }
                        }
                    }
                    Field("Ring markings") {
                        DeckSegmented(listOf("Plain", "Ticked", "Segmented"), s.ring.ordinal) { i ->
                            holder.update { it.copy(ring = RingDesign.entries[i]) }
                        }
                    }
                    Field("Center cap") {
                        DeckSegmented(listOf("Dome", "Flat", "Ringed"), s.cap.ordinal) { i ->
                            holder.update { it.copy(cap = CapDesign.entries[i]) }
                        }
                    }
                    SliderRow("Opacity", pct(s.opacity), s.opacity, JoystickLimits.Opacity,
                        { v -> holder.update { it.copy(opacity = v) } }, step = 0.01f)
                    SliderRow("Lighting", pct(s.lighting), s.lighting, JoystickLimits.Lighting,
                        { v -> holder.update { it.copy(lighting = v) } }, step = 0.01f)
                    Field("Accent color") {
                        Swatches(s.accentHue) { h -> holder.update { it.copy(accentHue = h) } }
                        DeckSlider(s.accentHue, { h -> holder.update { it.copy(accentHue = h) } }, JoystickLimits.Hue)
                    }
                }
            }
            item {
                Group("Feedback and calibration", ChamferShape(16.dp, topRight = true)) {
                    ToggleRow(
                        "Vibration", s.haptics && hasVibrator, { v -> holder.update { it.copy(haptics = v) } },
                        caption = if (hasVibrator) "A tick when you touch and when you reach the edge" else "This device reports no vibrator",
                        enabled = hasVibrator
                    )
                    ToggleRow(
                        "Touch feedback", s.touchFeedback, { v -> holder.update { it.copy(touchFeedback = v) } },
                        caption = "Knob presses in and lights its rim while touched"
                    )
                    Text(
                        "Calibration gain: ${f2(s.calibrationGain)}x. Use Calibrate above the stick.",
                        style = MaterialTheme.typography.bodySmall, color = Titanium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        KeyButton(
                            "Restore previous", { holder.replace(sessionStart) },
                            modifier = Modifier.weight(1f), enabled = s != sessionStart
                        )
                        KeyButton(
                            if (confirmReset) "Tap again to confirm" else "Reset to default",
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

private fun describeCentering(o: StickOutput, mode: ReturnMode): String = when {
    mode == ReturnMode.Hold -> "Held at X ${signed(o.x)}  Y ${signed(o.y)}"
    abs(o.x) < 0.001f && abs(o.y) < 0.001f -> "Returned to center: 0.00, 0.00"
    else -> "Off-center after release: ${signed(o.x)}, ${signed(o.y)}"
}

@Composable
private fun TopBar(saveFailed: Boolean, onBack: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KeyButton("Back", onBack, width = 64.dp)
        Spacer(Modifier.width(14.dp))
        Text("Joystick", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
        StatusLed(if (saveFailed) Orange else Lime)
        Spacer(Modifier.width(8.dp))
        Text(
            if (saveFailed) "Save failed" else "Auto-saved",
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
    status: String,
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
                    if (out.active) "${out.angleDeg.roundToInt()}\u00B0 ${JoystickMath.compass(out.angleDeg)}" else "idle",
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
                    status,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (statusWarn) Orange else Titanium,
                    maxLines = 2,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KeyButton("Peak ${f2(peak)}", onResetPeak, modifier = Modifier.weight(1f))
                KeyButton("Reset cal", onResetCalibration, modifier = Modifier.weight(1f), enabled = s.calibrationGain != 1f)
                KeyButton("Calibrate", onCalibrate, modifier = Modifier.weight(1f), enabled = !calibrating, highlight = calibrating)
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
    val presets = listOf(72f to "Lime", 187f to "Cyan", 24f to "Orange", 2f to "Red")
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        presets.forEach { (h, name) ->
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
