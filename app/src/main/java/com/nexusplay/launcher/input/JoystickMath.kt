package com.nexusplay.launcher.input

import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.pow

/**
 * One processed stick sample.
 * x, y: final output in -1..1 (right and up are positive).
 * inX, inY: knob position before processing, as a fraction of full travel (up is positive).
 */
data class StickOutput(
    val x: Float,
    val y: Float,
    val inX: Float,
    val inY: Float,
    val magnitude: Float,
    val angleDeg: Float,
    val rawMagnitude: Float,
    val active: Boolean
) {
    companion object {
        val Zero = StickOutput(0f, 0f, 0f, 0f, 0f, 0f, 0f, false)
    }
}

/** Pure math, no Android types, so it can be unit tested on the JVM. */
object JoystickMath {

    /**
     * Pipeline: circular clamp, calibration gain, radial deadzone (rescaled so output starts at 0
     * right outside the deadzone), response curve, sensitivity, per-axis clamp, inversion.
     * [knobX] and [knobY] are screen-space fractions of full travel (y grows downward).
     */
    fun compute(knobX: Float, knobY: Float, s: JoystickSettings): StickOutput {
        var ix = knobX
        var iy = -knobY
        val length = hypot(ix, iy)
        if (length > 1f) {
            ix /= length
            iy /= length
        }
        val raw = min(length, 1f)

        var vx = ix * s.calibrationGain
        var vy = iy * s.calibrationGain
        var mag = hypot(vx, vy)
        if (mag > 1f) {
            vx /= mag
            vy /= mag
            mag = 1f
        }
        if (mag <= 0f || mag <= s.deadzone) {
            return StickOutput(0f, 0f, ix, iy, 0f, 0f, raw, false)
        }

        val dirX = vx / mag
        val dirY = vy / mag
        var m = ((mag - s.deadzone) / (1f - s.deadzone)).coerceIn(0f, 1f)
        if (s.curve == ResponseCurve.Exponential) m = m.pow(s.curveExponent)

        var ox = (dirX * m * s.sensitivity * s.sensitivityX).coerceIn(-1f, 1f)
        var oy = (dirY * m * s.sensitivity * s.sensitivityY).coerceIn(-1f, 1f)
        if (s.invertX) ox = -ox
        if (s.invertY) oy = -oy

        var angle = Math.toDegrees(atan2(oy, ox).toDouble()).toFloat()
        if (angle < 0f) angle += 360f
        return StickOutput(ox, oy, ix, iy, min(1f, hypot(ox, oy)), angle, raw, true)
    }

    /** 8-way direction index for an angle in degrees: 0 = right, then counter-clockwise in 45 degree steps. */
    fun compassIndex(angleDeg: Float): Int = ((angleDeg + 22.5f) / 45f).toInt() % 8
}
