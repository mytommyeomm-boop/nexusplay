package com.nexusplay.launcher.input

import org.json.JSONObject

/**
 * How raw finger movement is turned into axis values.
 *  - Linear:    straight response, the curve setting is ignored.
 *  - Curve:     response follows [JoystickConfig.curve] (below 1 = more sensitive near center).
 *  - Smooth:    same curve, plus low-pass filtering so the output eases toward the target.
 *  - Precision: reduced gain and a flatter start, for fine aiming.
 *  - Fast:      boosted gain and a steeper start, reaches full output quickly.
 */
enum class ResponseMode(val label: String, val usesCurve: Boolean) {
    Linear("Linear", false),
    Curve("Curve", true),
    Smooth("Smooth", true),
    Precision("Precision", false),
    Fast("Fast", false)
}

/** Slider limits. The UI and [JoystickConfig.sanitized] share these so they can never disagree. */
object JoystickLimits {
    val size = 90f..180f          // base diameter, dp
    val opacity = 0.2f..1f
    val position = 0.1f..0.9f     // fraction of the control zone
    val sensitivity = 0.2f..3f
    val range = 0.6f..1.4f        // finger travel needed for full deflection, relative to base radius
    val innerDeadzone = 0f..0.5f
    val outerDeadzone = 0f..0.3f
    val curve = 0.5f..3f
    val acceleration = 0f..1f
    val returnSpeed = 0f..1f      // 1 = instant, 0 = slowest (500 ms)
}

data class JoystickConfig(
    val enabled: Boolean = true,
    val sizeDp: Float = 130f,
    val opacity: Float = 0.85f,
    val posX: Float = 0.22f,
    val posY: Float = 0.68f,
    val sensitivity: Float = 1f,
    val sensitivityX: Float = 1f,
    val sensitivityY: Float = 1f,
    val range: Float = 1f,
    val innerDeadzone: Float = 0.08f,
    val outerDeadzone: Float = 0.03f,
    val returnToCenter: Boolean = true,
    val returnSpeed: Float = 1f,
    val mode: ResponseMode = ResponseMode.Linear,
    val curve: Float = 1.5f,
    val acceleration: Float = 0f,
    val invertX: Boolean = false,
    val invertY: Boolean = false,
    val lockX: Boolean = false,
    val lockY: Boolean = false,
    val floating: Boolean = false,
    val vibration: Boolean = false
) {
    fun sanitized(): JoystickConfig = copy(
        sizeDp = sizeDp.coerceIn(JoystickLimits.size),
        opacity = opacity.coerceIn(JoystickLimits.opacity),
        posX = posX.coerceIn(JoystickLimits.position),
        posY = posY.coerceIn(JoystickLimits.position),
        sensitivity = sensitivity.coerceIn(JoystickLimits.sensitivity),
        sensitivityX = sensitivityX.coerceIn(JoystickLimits.sensitivity),
        sensitivityY = sensitivityY.coerceIn(JoystickLimits.sensitivity),
        range = range.coerceIn(JoystickLimits.range),
        innerDeadzone = innerDeadzone.coerceIn(JoystickLimits.innerDeadzone),
        outerDeadzone = outerDeadzone.coerceIn(JoystickLimits.outerDeadzone),
        returnSpeed = returnSpeed.coerceIn(JoystickLimits.returnSpeed),
        curve = curve.coerceIn(JoystickLimits.curve),
        acceleration = acceleration.coerceIn(JoystickLimits.acceleration)
    )

    fun toJson(): JSONObject = JSONObject().apply {
        put("enabled", enabled); put("sizeDp", sizeDp.toDouble()); put("opacity", opacity.toDouble())
        put("posX", posX.toDouble()); put("posY", posY.toDouble())
        put("sensitivity", sensitivity.toDouble()); put("sensitivityX", sensitivityX.toDouble())
        put("sensitivityY", sensitivityY.toDouble()); put("range", range.toDouble())
        put("innerDeadzone", innerDeadzone.toDouble()); put("outerDeadzone", outerDeadzone.toDouble())
        put("returnToCenter", returnToCenter); put("returnSpeed", returnSpeed.toDouble())
        put("mode", mode.name); put("curve", curve.toDouble()); put("acceleration", acceleration.toDouble())
        put("invertX", invertX); put("invertY", invertY); put("lockX", lockX); put("lockY", lockY)
        put("floating", floating); put("vibration", vibration)
    }

    companion object {
        /** Tolerant reader: missing or invalid fields fall back to defaults, values are clamped. */
        fun fromJson(o: JSONObject): JoystickConfig {
            val d = JoystickConfig()
            fun f(key: String, def: Float) = o.optDouble(key, def.toDouble()).let { if (it.isNaN()) def else it.toFloat() }
            return JoystickConfig(
                enabled = o.optBoolean("enabled", d.enabled),
                sizeDp = f("sizeDp", d.sizeDp), opacity = f("opacity", d.opacity),
                posX = f("posX", d.posX), posY = f("posY", d.posY),
                sensitivity = f("sensitivity", d.sensitivity),
                sensitivityX = f("sensitivityX", d.sensitivityX),
                sensitivityY = f("sensitivityY", d.sensitivityY),
                range = f("range", d.range),
                innerDeadzone = f("innerDeadzone", d.innerDeadzone),
                outerDeadzone = f("outerDeadzone", d.outerDeadzone),
                returnToCenter = o.optBoolean("returnToCenter", d.returnToCenter),
                returnSpeed = f("returnSpeed", d.returnSpeed),
                mode = ResponseMode.entries.firstOrNull { it.name == o.optString("mode") } ?: d.mode,
                curve = f("curve", d.curve), acceleration = f("acceleration", d.acceleration),
                invertX = o.optBoolean("invertX", d.invertX), invertY = o.optBoolean("invertY", d.invertY),
                lockX = o.optBoolean("lockX", d.lockX), lockY = o.optBoolean("lockY", d.lockY),
                floating = o.optBoolean("floating", d.floating),
                vibration = o.optBoolean("vibration", d.vibration)
            ).sanitized()
        }
    }
}
