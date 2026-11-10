package com.nexusplay.launcher.input

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JoystickMathTest {
    private val base = JoystickSettings(deadzone = 0.10f)
    private val eps = 1e-4f

    @Test fun centerGivesZero() {
        val o = JoystickMath.compute(0f, 0f, base)
        assertEquals(0f, o.x, 0f); assertEquals(0f, o.y, 0f); assertFalse(o.active)
    }

    @Test fun insideDeadzoneGivesZero() {
        val o = JoystickMath.compute(0.05f, 0.05f, base)
        assertEquals(0f, o.x, 0f); assertEquals(0f, o.y, 0f); assertFalse(o.active)
    }

    @Test fun linearOutputStartsAtZeroOutsideDeadzone() {
        assertEquals(0.5f, JoystickMath.compute(0.55f, 0f, base).x, eps)
    }

    @Test fun fullDeflectionGivesOne() {
        assertEquals(1f, JoystickMath.compute(1f, 0f, base).x, eps)
        assertEquals(1f, JoystickMath.compute(5f, 0f, base).x, eps)
    }

    @Test fun screenUpIsPositiveYAndInvertFlipsIt() {
        assertEquals(0.5f, JoystickMath.compute(0f, -0.55f, base).y, eps)
        assertEquals(-0.5f, JoystickMath.compute(0f, -0.55f, base.copy(invertY = true)).y, eps)
    }

    @Test fun invertXFlipsX() {
        assertEquals(-0.5f, JoystickMath.compute(0.55f, 0f, base.copy(invertX = true)).x, eps)
    }

    @Test fun exponentialCurveSquaresTheScaledMagnitude() {
        val s = base.copy(curve = ResponseCurve.Exponential, curveExponent = 2f)
        assertEquals(0.25f, JoystickMath.compute(0.55f, 0f, s).x, eps)
    }

    @Test fun axisSensitivityIsIndependent() {
        val s = JoystickSettings(deadzone = 0f, sensitivityX = 2f)
        val o = JoystickMath.compute(0.3f, -0.3f, s)
        assertEquals(0.6f, o.x, eps)
        assertEquals(0.3f, o.y, eps)
    }

    @Test fun outputIsClamped() {
        val s = JoystickSettings(deadzone = 0f, sensitivity = 2f)
        assertEquals(1f, JoystickMath.compute(1f, 0f, s).x, eps)
    }

    @Test fun calibrationGainLetsPartialReachGiveFullOutput() {
        val s = JoystickSettings(deadzone = 0f, calibrationGain = 1.25f)
        assertEquals(1f, JoystickMath.compute(0.8f, 0f, s).x, eps)
    }

    @Test fun directionIndex() {
        assertEquals(0, JoystickMath.compassIndex(0f))     // right
        assertEquals(2, JoystickMath.compassIndex(90f))    // up
        assertEquals(5, JoystickMath.compassIndex(225f))   // down-left
        assertEquals(0, JoystickMath.compassIndex(359f))   // wraps back to right
    }

    @Test fun sanitizeFixesBadValues() {
        val s = JoystickSettings(sensitivity = Float.NaN, deadzone = 9f, sizeDp = -5f).sanitized()
        assertEquals(1f, s.sensitivity, 0f)
        assertEquals(0.5f, s.deadzone, 0f)
        assertEquals(100f, s.sizeDp, 0f)
    }

    @Test fun jsonRoundTripKeepsEveryField() {
        val s = JoystickSettings(
            sensitivity = 1.37f, sensitivityY = 0.8f, deadzone = 0.21f, mode = StickMode.Floating,
            side = StickSide.Right, curve = ResponseCurve.Exponential, invertY = true,
            returnMode = ReturnMode.Hold, style = StickStyle.Futuristic, cap = CapDesign.Ringed,
            accentHue = 187f, haptics = false, calibrationGain = 1.2f
        )
        assertEquals(s, JoystickSettings.fromJson(JSONObject(s.toJson().toString())))
    }

    @Test fun unknownEnumFallsBackToDefault() {
        val j = JoystickSettings().toJson().put("style", "Nonsense")
        assertTrue(JoystickSettings.fromJson(j).style == StickStyle.Metallic)
    }
}
