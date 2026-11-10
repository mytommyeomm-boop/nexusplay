package com.nexusplay.launcher.ui.joystick

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.nexusplay.launcher.input.CapDesign
import com.nexusplay.launcher.input.JoystickSettings
import com.nexusplay.launcher.input.RingDesign
import com.nexusplay.launcher.input.StickOutput
import com.nexusplay.launcher.input.StickStyle
import com.nexusplay.launcher.ui.theme.Graphite0
import com.nexusplay.launcher.ui.theme.Graphite1
import com.nexusplay.launcher.ui.theme.Graphite3
import com.nexusplay.launcher.ui.theme.Titanium
import kotlin.math.cos
import kotlin.math.sin

fun accentOf(s: JoystickSettings): Color = Color.hsv(s.accentHue.coerceIn(0f, 360f), 0.85f, 1f)

/**
 * Draws one stick. Everything is vector drawing (no bitmaps), light comes from the top left.
 * [knob] is the knob offset from [center] in pixels; [ghost] fades the whole stick (floating idle).
 */
fun DrawScope.drawStick(
    center: Offset,
    baseR: Float,
    knob: Offset,
    travelPx: Float,
    s: JoystickSettings,
    pressed: Boolean,
    out: StickOutput?,
    showDeadzone: Boolean,
    ghost: Float = 1f
) {
    val accent = accentOf(s)
    val light = s.lighting
    val g = ghost
    val ringT = baseR * 0.14f
    val dash = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 5.dp.toPx()))

    // Contact shadow
    val shadowC = center + Offset(0f, baseR * 0.06f)
    drawCircle(
        Brush.radialGradient(listOf(Color.Black.copy(alpha = 0.55f * g), Color.Transparent), center = shadowC, radius = baseR * 1.12f),
        radius = baseR * 1.12f,
        center = shadowC
    )

    // Outer ring
    when (s.style) {
        StickStyle.Metallic -> {
            val metal = Brush.sweepGradient(
                0.00f to Color(0xFFE9EDF2), 0.12f to Color(0xFF8E959F), 0.25f to Color(0xFFD7DCE3),
                0.38f to Color(0xFF5C626B), 0.50f to Color(0xFFC4C9D0), 0.62f to Color(0xFF666C75),
                0.75f to Color(0xFFE1E5EA), 0.88f to Color(0xFF7A808A), 1.00f to Color(0xFFE9EDF2),
                center = center
            )
            drawCircle(metal, baseR - ringT / 2f, center, alpha = g, style = Stroke(ringT))
            drawCircle(Color.White.copy(alpha = 0.4f * g), baseR - 0.5.dp.toPx(), center, style = Stroke(1.dp.toPx()))
            drawCircle(Color.Black.copy(alpha = 0.5f * g), baseR - ringT, center, style = Stroke(1.5.dp.toPx()))
        }
        StickStyle.Minimal -> {
            drawCircle(Titanium.copy(alpha = 0.9f * g), baseR - 1.dp.toPx(), center, style = Stroke(2.dp.toPx()))
        }
        StickStyle.Futuristic -> {
            drawCircle(Graphite1.copy(alpha = g), baseR - ringT / 2f, center, style = Stroke(ringT))
            drawCircle(accent.copy(alpha = (0.35f + 0.5f * light) * g), baseR - 1.dp.toPx(), center, style = Stroke(1.5.dp.toPx()))
            drawCircle(Titanium.copy(alpha = 0.5f * g), baseR - ringT, center, style = Stroke(1.dp.toPx()))
        }
    }

    // Ring markings
    val markR = baseR - ringT * 1.7f
    when (s.ring) {
        RingDesign.Ticked -> repeat(36) { i ->
            val a = Math.toRadians(i * 10.0)
            val long = i % 3 == 0
            val inner = markR - (if (long) 7.dp else 4.dp).toPx()
            val dx = cos(a).toFloat()
            val dy = sin(a).toFloat()
            drawLine(
                Titanium.copy(alpha = (if (long) 0.75f else 0.4f) * g),
                center + Offset(dx * inner, dy * inner),
                center + Offset(dx * markR, dy * markR),
                1.dp.toPx()
            )
        }
        RingDesign.Segmented -> repeat(8) { i ->
            drawArc(
                Titanium.copy(alpha = 0.6f * g),
                startAngle = i * 45f + 4.5f,
                sweepAngle = 36f,
                useCenter = false,
                topLeft = center - Offset(markR, markR),
                size = Size(markR * 2f, markR * 2f),
                style = Stroke(3.dp.toPx())
            )
        }
        RingDesign.Plain -> Unit
    }

    // Base plate
    val plateR = baseR - ringT
    val plate: Brush = if (s.style == StickStyle.Minimal) SolidColor(Graphite1)
    else Brush.radialGradient(
        listOf(Graphite3, Graphite0),
        center = center + Offset(-plateR * 0.3f, -plateR * 0.35f),
        radius = plateR * 1.5f
    )
    drawCircle(plate, plateR - 1f, center, alpha = g)

    // Maximum movement radius
    drawCircle(Color.White.copy(alpha = 0.14f * g), travelPx, center, style = Stroke(1.dp.toPx(), pathEffect = dash))

    // Deadzone (input space, so the calibration gain is divided out)
    if (showDeadzone && s.deadzone > 0f) {
        val dr = s.deadzone / s.calibrationGain * travelPx
        drawCircle(accent.copy(alpha = 0.10f * g), dr, center)
        drawCircle(accent.copy(alpha = 0.7f * g), dr, center, style = Stroke(1.dp.toPx(), pathEffect = dash))
    }

    // Direction light on the inner edge
    if (out != null && out.active && light > 0f) {
        val glowR = plateR - 2.dp.toPx()
        val a = (0.25f + 0.75f * out.magnitude) * light
        val start = -out.angleDeg
        drawArc(
            accent.copy(alpha = a * 0.35f * g), start - 38f, 76f, false,
            topLeft = center - Offset(glowR, glowR), size = Size(glowR * 2f, glowR * 2f),
            style = Stroke(ringT * 0.9f, cap = StrokeCap.Round)
        )
        drawArc(
            accent.copy(alpha = a * g), start - 26f, 52f, false,
            topLeft = center - Offset(glowR, glowR), size = Size(glowR * 2f, glowR * 2f),
            style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round)
        )
    }

    // Knob
    val kr = baseR * 0.36f * (if (pressed && s.touchFeedback) 0.94f else 1f)
    val kc = center + knob
    drawCircle(Color.Black.copy(alpha = 0.45f * g), kr * 1.04f, kc + Offset(0f, kr * 0.18f))
    val flat = s.cap == CapDesign.Flat || s.style == StickStyle.Minimal
    val body: Brush = when {
        s.style == StickStyle.Minimal -> SolidColor(Titanium)
        s.cap == CapDesign.Flat -> SolidColor(Graphite3)
        else -> Brush.radialGradient(
            listOf(Color(0xFFF3F6F9), Color(0xFF9AA1AC), Color(0xFF3A3F47)),
            center = kc + Offset(-kr * 0.3f, -kr * 0.35f),
            radius = kr * 1.35f
        )
    }
    drawCircle(body, kr, kc, alpha = g)
    drawCircle(
        Brush.linearGradient(
            listOf(Color.White.copy(alpha = 0.7f), Color.Black.copy(alpha = 0.6f)),
            start = kc - Offset(kr, kr), end = kc + Offset(kr, kr)
        ),
        kr - 0.5.dp.toPx(), kc, alpha = g, style = Stroke(1.5.dp.toPx())
    )
    if (s.cap == CapDesign.Ringed) {
        drawCircle(Color.Black.copy(alpha = 0.35f * g), kr * 0.72f, kc, style = Stroke(1.2.dp.toPx()))
        drawCircle(Color.Black.copy(alpha = 0.30f * g), kr * 0.46f, kc, style = Stroke(1.2.dp.toPx()))
        drawCircle(accent.copy(alpha = (0.3f + 0.6f * light) * g), kr * 0.6f, kc, style = Stroke(1.5.dp.toPx()))
    }
    if (s.cap == CapDesign.Flat) {
        drawCircle(accent.copy(alpha = 0.6f * g), kr * 0.8f, kc, style = Stroke(1.5.dp.toPx()))
    }
    if (!flat) {
        val hc = kc + Offset(-kr * 0.35f, -kr * 0.4f)
        drawCircle(
            Brush.radialGradient(listOf(Color.White.copy(alpha = 0.55f * light * g), Color.Transparent), center = hc, radius = kr * 0.45f),
            kr * 0.45f, hc
        )
    }
    if (pressed && s.touchFeedback) {
        drawCircle(accent.copy(alpha = 0.8f * g), kr, kc, style = Stroke(2.dp.toPx()))
    }
}
