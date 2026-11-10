package com.nexusplay.launcher.input

import org.json.JSONObject

enum class StickMode { Fixed, Floating }
enum class StickSide { Left, Right }
enum class ResponseMode { Responsive, Smooth }
enum class ResponseCurve { Linear, Exponential }
enum class ReturnMode { Spring, Soft, Snap, Hold }
enum class StickStyle { Metallic, Minimal, Futuristic }
enum class RingDesign { Plain, Ticked, Segmented }
enum class CapDesign { Dome, Flat, Ringed }

/** Allowed ranges, shared by sanitizing and by the sliders so they can never disagree. */
object JoystickLimits {
    val Sensitivity = 0.2f..2f
    val Deadzone = 0f..0.5f
    val Size = 100f..190f
    val Travel = 0.5f..1f
    val Exponent = 1.2f..3f
    val Opacity = 0.2f..1f
    val Lighting = 0f..1f
    val Hue = 0f..360f
    val Gain = 1f..1.6f
    val Position = 0.08f..0.92f
}

/** Everything the user can tune about one virtual stick. Immutable; the UI copies it. */
data class JoystickSettings(
    val sensitivity: Float = 1f,
    val sensitivityX: Float = 1f,
    val sensitivityY: Float = 1f,
    val deadzone: Float = 0.10f,
    val sizeDp: Float = 160f,
    val travel: Float = 0.80f,
    val mode: StickMode = StickMode.Fixed,
    val side: StickSide = StickSide.Left,
    val positionX: Float = 0.18f,
    val positionY: Float = 0.70f,
    val response: ResponseMode = ResponseMode.Responsive,
    val curve: ResponseCurve = ResponseCurve.Linear,
    val curveExponent: Float = 2f,
    val invertX: Boolean = false,
    val invertY: Boolean = false,
    val returnMode: ReturnMode = ReturnMode.Spring,
    val style: StickStyle = StickStyle.Metallic,
    val ring: RingDesign = RingDesign.Ticked,
    val cap: CapDesign = CapDesign.Dome,
    val opacity: Float = 0.9f,
    val accentHue: Float = 72f,
    val lighting: Float = 0.6f,
    val haptics: Boolean = true,
    val touchFeedback: Boolean = true,
    val calibrationGain: Float = 1f
) {
    /** Clamps every value into its legal range; NaN and infinity fall back to the default. */
    fun sanitized(): JoystickSettings {
        val d = JoystickSettings()
        return copy(
            sensitivity = sensitivity.fix(JoystickLimits.Sensitivity, d.sensitivity),
            sensitivityX = sensitivityX.fix(JoystickLimits.Sensitivity, d.sensitivityX),
            sensitivityY = sensitivityY.fix(JoystickLimits.Sensitivity, d.sensitivityY),
            deadzone = deadzone.fix(JoystickLimits.Deadzone, d.deadzone),
            sizeDp = sizeDp.fix(JoystickLimits.Size, d.sizeDp),
            travel = travel.fix(JoystickLimits.Travel, d.travel),
            positionX = positionX.fix(JoystickLimits.Position, d.positionX),
            positionY = positionY.fix(JoystickLimits.Position, d.positionY),
            curveExponent = curveExponent.fix(JoystickLimits.Exponent, d.curveExponent),
            opacity = opacity.fix(JoystickLimits.Opacity, d.opacity),
            accentHue = accentHue.fix(JoystickLimits.Hue, d.accentHue),
            lighting = lighting.fix(JoystickLimits.Lighting, d.lighting),
            calibrationGain = calibrationGain.fix(JoystickLimits.Gain, d.calibrationGain)
        )
    }

    fun toJson(): JSONObject = JSONObject().apply {
        put("version", 1)
        put("sensitivity", sensitivity.toDouble())
        put("sensitivityX", sensitivityX.toDouble())
        put("sensitivityY", sensitivityY.toDouble())
        put("deadzone", deadzone.toDouble())
        put("sizeDp", sizeDp.toDouble())
        put("travel", travel.toDouble())
        put("mode", mode.name)
        put("side", side.name)
        put("positionX", positionX.toDouble())
        put("positionY", positionY.toDouble())
        put("response", response.name)
        put("curve", curve.name)
        put("curveExponent", curveExponent.toDouble())
        put("invertX", invertX)
        put("invertY", invertY)
        put("returnMode", returnMode.name)
        put("style", style.name)
        put("ring", ring.name)
        put("cap", cap.name)
        put("opacity", opacity.toDouble())
        put("accentHue", accentHue.toDouble())
        put("lighting", lighting.toDouble())
        put("haptics", haptics)
        put("touchFeedback", touchFeedback)
        put("calibrationGain", calibrationGain.toDouble())
    }

    companion object {
        /** Missing or unknown fields fall back to defaults, so older files keep loading. */
        fun fromJson(j: JSONObject): JoystickSettings {
            val d = JoystickSettings()
            return JoystickSettings(
                sensitivity = j.optF("sensitivity", d.sensitivity),
                sensitivityX = j.optF("sensitivityX", d.sensitivityX),
                sensitivityY = j.optF("sensitivityY", d.sensitivityY),
                deadzone = j.optF("deadzone", d.deadzone),
                sizeDp = j.optF("sizeDp", d.sizeDp),
                travel = j.optF("travel", d.travel),
                mode = j.optEnum("mode", d.mode),
                side = j.optEnum("side", d.side),
                positionX = j.optF("positionX", d.positionX),
                positionY = j.optF("positionY", d.positionY),
                response = j.optEnum("response", d.response),
                curve = j.optEnum("curve", d.curve),
                curveExponent = j.optF("curveExponent", d.curveExponent),
                invertX = j.optBoolean("invertX", d.invertX),
                invertY = j.optBoolean("invertY", d.invertY),
                returnMode = j.optEnum("returnMode", d.returnMode),
                style = j.optEnum("style", d.style),
                ring = j.optEnum("ring", d.ring),
                cap = j.optEnum("cap", d.cap),
                opacity = j.optF("opacity", d.opacity),
                accentHue = j.optF("accentHue", d.accentHue),
                lighting = j.optF("lighting", d.lighting),
                haptics = j.optBoolean("haptics", d.haptics),
                touchFeedback = j.optBoolean("touchFeedback", d.touchFeedback),
                calibrationGain = j.optF("calibrationGain", d.calibrationGain)
            ).sanitized()
        }
    }
}

private fun Float.fix(range: ClosedFloatingPointRange<Float>, fallback: Float): Float =
    if (isNaN() || isInfinite()) fallback else coerceIn(range.start, range.endInclusive)

private fun JSONObject.optF(key: String, default: Float): Float =
    optDouble(key, default.toDouble()).toFloat()

private inline fun <reified T : Enum<T>> JSONObject.optEnum(key: String, default: T): T =
    runCatching { enumValueOf<T>(optString(key, default.name)) }.getOrDefault(default)
