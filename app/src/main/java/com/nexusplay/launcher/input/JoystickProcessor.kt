package com.nexusplay.launcher.input

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.pow

data class AxisPair(val x: Float, val y: Float)

/**
 * One snapshot of the stick. [x]/[y] are the final gamepad values in -1..1 with the
 * XInput convention (up = +y). [thumbX]/[thumbY] are the finger position for drawing,
 * normalized to the travel limit, in screen orientation (down = +y).
 */
data class JoystickOutput(
    val x: Float = 0f,
    val y: Float = 0f,
    val magnitude: Float = 0f,
    val angleDeg: Float = 0f,
    val direction: String = "CENTER",
    val thumbX: Float = 0f,
    val thumbY: Float = 0f,
    val active: Boolean = false
)

/** Pure math, no Android types, so the same function drives the stick, the test area and the curve graph. */
object JoystickMath {
    private val directions = listOf("RIGHT", "UP-RIGHT", "UP", "UP-LEFT", "LEFT", "DOWN-LEFT", "DOWN", "DOWN-RIGHT")

    fun exponentFor(c: JoystickConfig): Float = when (c.mode) {
        ResponseMode.Linear -> 1f
        ResponseMode.Curve, ResponseMode.Smooth -> c.curve
        ResponseMode.Precision -> 1.6f
        ResponseMode.Fast -> 0.75f
    }

    fun gainFor(c: JoystickConfig): Float = when (c.mode) {
        ResponseMode.Precision -> 0.6f
        ResponseMode.Fast -> 1.3f
        else -> 1f
    }

    /**
     * rawX/rawY: finger deflection normalized to -1..1, screen orientation (down = +y).
     * Returns gamepad-convention axes (up = +y). Order: axis locks, radial deadzones,
     * response curve, gain, sensitivity, per-axis sensitivity, inversion.
     * Smoothing and acceleration ramp are time-based and applied by [JoystickProcessor].
     */
    fun shape(rawX: Float, rawY: Float, c: JoystickConfig): AxisPair {
        val sx = if (c.lockX) 0f else rawX
        val sy = if (c.lockY) 0f else rawY
        val mag = hypot(sx, sy)
        if (mag <= 1e-6f) return AxisPair(0f, 0f)

        val inner = c.innerDeadzone
        val outerStart = max(1f - c.outerDeadzone, inner + 0.1f)
        var m = ((mag - inner) / (outerStart - inner)).coerceIn(0f, 1f)
        if (m == 0f) return AxisPair(0f, 0f)

        m = m.pow(exponentFor(c)) * gainFor(c) * c.sensitivity
        var ox = (sx / mag * m * c.sensitivityX).coerceIn(-1f, 1f)
        var oy = (-sy / mag * m * c.sensitivityY).coerceIn(-1f, 1f)
        if (c.invertX) ox = -ox
        if (c.invertY) oy = -oy
        return AxisPair(ox, oy)
    }

    fun directionOf(x: Float, y: Float): String {
        if (hypot(x, y) < 0.15f) return "CENTER"
        var deg = Math.toDegrees(atan2(y.toDouble(), x.toDouble()))
        if (deg < 0) deg += 360.0
        return directions[((deg + 22.5) / 45.0).toInt() % 8]
    }
}

/**
 * Single-pointer analog stick state machine. The UI owns pointer-id tracking and feeds
 * [down]/[move]/[up] in pixels plus a monotonic millisecond clock; call [update] once per frame
 * while [needsFrame] is true. Nothing here touches Android, so it is unit-testable on the JVM.
 */
class JoystickProcessor {
    var config: JoystickConfig = JoystickConfig()
    /** Base radius in px (half of the configured size). Travel limit is radius * config.range. */
    var radiusPx: Float = 100f

    var baseX = 0f
        private set
    var baseY = 0f
        private set
    var touching = false
        private set

    private var rawX = 0f
    private var rawY = 0f
    private var returning = false
    private var returnFromX = 0f
    private var returnFromY = 0f
    private var returnStartMs = 0L
    private var downMs = 0L
    private var lastMs = -1L
    private var smX = 0f
    private var smY = 0f
    private var busy = false
    private var hapticPending = false
    private var atEdge = false

    private val travel: Float get() = radiusPx * config.range
    private fun returnMs(): Float = (1f - config.returnSpeed) * 500f

    /** Returns false when the touch should be ignored (disabled, or outside the base in fixed mode). */
    fun down(x: Float, y: Float, nowMs: Long, fixedCx: Float, fixedCy: Float): Boolean {
        if (!config.enabled) return false
        if (config.floating) {
            baseX = x; baseY = y
        } else {
            baseX = fixedCx; baseY = fixedCy
            if (hypot(x - baseX, y - baseY) > max(radiusPx, travel) * 1.3f) return false
        }
        touching = true; returning = false; busy = true; downMs = nowMs
        if (config.vibration) hapticPending = true
        track(x, y)
        return true
    }

    fun move(x: Float, y: Float) {
        if (!touching) return
        busy = true
        track(x, y)
    }

    fun up(nowMs: Long) {
        if (!touching) return
        touching = false; atEdge = false; busy = true
        if (config.returnToCenter) {
            val ms = returnMs()
            if (ms <= 0f || (rawX == 0f && rawY == 0f)) {
                rawX = 0f; rawY = 0f
            } else {
                returning = true; returnFromX = rawX; returnFromY = rawY; returnStartMs = nowMs
            }
        }
        // returnToCenter = false: the stick deliberately holds its last position until reset().
    }

    /** Hard reset, used when the control is disabled, hidden or the screen is left. */
    fun reset() {
        touching = false; returning = false; busy = false; atEdge = false
        rawX = 0f; rawY = 0f; smX = 0f; smY = 0f
    }

    fun needsFrame(): Boolean = touching || returning || busy

    fun takeHaptic(): Boolean = hapticPending.also { hapticPending = false }

    fun update(nowMs: Long): JoystickOutput {
        val dt = if (lastMs < 0) 16L else (nowMs - lastMs).coerceIn(0L, 100L)
        lastMs = nowMs

        if (returning) {
            val dur = returnMs()
            val t = if (dur <= 0f) 1f else ((nowMs - returnStartMs) / dur).coerceIn(0f, 1f)
            if (t >= 1f) {
                rawX = 0f; rawY = 0f; returning = false
            } else {
                rawX = returnFromX * (1f - t); rawY = returnFromY * (1f - t)
            }
        }

        val target = JoystickMath.shape(rawX, rawY, config)
        var tx = target.x
        var ty = target.y
        if (touching && config.acceleration > 0f) {
            val rampMs = config.acceleration * 400f
            val f = ((nowMs - downMs) / rampMs).coerceIn(0f, 1f)
            tx *= f; ty *= f
        }

        if (config.mode == ResponseMode.Smooth) {
            val a = 1f - exp(-dt / 60f)
            smX += (tx - smX) * a
            smY += (ty - smY) * a
            busy = abs(smX - tx) > 0.002f || abs(smY - ty) > 0.002f
            if (!busy) { smX = tx; smY = ty }
        } else {
            smX = tx; smY = ty; busy = false
        }

        val mag = hypot(smX, smY).coerceAtMost(1f)
        var angle = Math.toDegrees(atan2(smY.toDouble(), smX.toDouble())).toFloat()
        if (angle < 0f) angle += 360f
        return JoystickOutput(
            x = smX, y = smY, magnitude = mag, angleDeg = angle,
            direction = JoystickMath.directionOf(smX, smY),
            thumbX = if (config.lockX) 0f else rawX,
            thumbY = if (config.lockY) 0f else rawY,
            active = touching
        )
    }

    private fun track(x: Float, y: Float) {
        val t = travel
        var dx = (x - baseX) / t
        var dy = (y - baseY) / t
        val len = hypot(dx, dy)
        if (len > 1f) { dx /= len; dy /= len }
        rawX = dx; rawY = dy
        val m = hypot(dx, dy)
        if (m >= 0.95f && !atEdge) {
            atEdge = true
            if (config.vibration) hapticPending = true
        } else if (m < 0.85f) atEdge = false
    }
}
