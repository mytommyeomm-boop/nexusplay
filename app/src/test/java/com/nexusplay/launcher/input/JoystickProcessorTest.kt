package com.nexusplay.launcher.input

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JoystickProcessorTest {
    private val eps = 1e-3f

    private fun proc(c: JoystickConfig = JoystickConfig()) = JoystickProcessor().apply { config = c; radiusPx = 100f }

    @Test fun insideInnerDeadzoneIsZero() {
        val c = JoystickConfig(innerDeadzone = 0.2f)
        assertEquals(0f, JoystickMath.shape(0.1f, 0f, c).x, eps)
        assertTrue(JoystickMath.shape(0.3f, 0f, c).x > 0f)
    }

    @Test fun fullDeflectionIsOneAndUpIsPositiveY() {
        val c = JoystickConfig()
        assertEquals(1f, JoystickMath.shape(1f, 0f, c).x, eps)
        assertEquals(1f, JoystickMath.shape(0f, -1f, c).y, eps) // finger moved up on screen
    }

    @Test fun outerDeadzoneSaturatesEarly() {
        val c = JoystickConfig(outerDeadzone = 0.3f, innerDeadzone = 0f)
        assertEquals(1f, JoystickMath.shape(0.7f, 0f, c).x, eps)
    }

    @Test fun sensitivityScalesOutput() {
        val one = JoystickMath.shape(0.3f, 0f, JoystickConfig(sensitivity = 1f)).x
        val two = JoystickMath.shape(0.3f, 0f, JoystickConfig(sensitivity = 2f)).x
        assertEquals(one * 2f, two, 1e-4f)
    }

    @Test fun perAxisSensitivity() {
        val c = JoystickConfig(sensitivityX = 0.5f)
        assertEquals(0.5f, JoystickMath.shape(1f, 0f, c).x, eps)
    }

    @Test fun inversionAndLocks() {
        assertEquals(-1f, JoystickMath.shape(0f, -1f, JoystickConfig(invertY = true)).y, eps)
        assertEquals(-1f, JoystickMath.shape(1f, 0f, JoystickConfig(invertX = true)).x, eps)
        val locked = JoystickMath.shape(0.8f, -0.8f, JoystickConfig(lockX = true))
        assertEquals(0f, locked.x, eps)
        assertTrue(locked.y > 0f)
    }

    @Test fun precisionModeOutputsLessThanFastAtSameDeflection() {
        val p = JoystickMath.shape(0.5f, 0f, JoystickConfig(mode = ResponseMode.Precision)).x
        val f = JoystickMath.shape(0.5f, 0f, JoystickConfig(mode = ResponseMode.Fast)).x
        assertTrue(p < f)
    }

    @Test fun fixedStickTracksFingerAndReleasesInstantly() {
        val p = proc()
        assertTrue(p.down(300f, 300f, 0, 300f, 300f))
        p.move(400f, 300f)
        assertEquals(1f, p.update(16).x, eps)
        p.up(32)
        val out = p.update(48)
        assertEquals(0f, out.x, eps)
        assertFalse(out.active)
        assertFalse(p.needsFrame())
    }

    @Test fun fixedStickIgnoresTouchesFarFromBase() {
        assertFalse(proc().down(900f, 900f, 0, 300f, 300f))
    }

    @Test fun floatingStickCentersOnTouch() {
        val p = proc(JoystickConfig(floating = true))
        assertTrue(p.down(50f, 60f, 0, 300f, 300f))
        p.move(150f, 60f)
        assertEquals(1f, p.update(16).x, eps)
    }

    @Test fun disabledStickRejectsTouch() {
        assertFalse(proc(JoystickConfig(enabled = false)).down(300f, 300f, 0, 300f, 300f))
    }

    @Test fun returnAnimationDecaysToZero() {
        val p = proc(JoystickConfig(returnSpeed = 0.5f)) // 250 ms
        p.down(300f, 300f, 0, 300f, 300f); p.move(400f, 300f); p.update(16)
        p.up(1000)
        val mid = p.update(1125).x
        assertTrue(mid > 0.3f && mid < 0.6f)
        assertEquals(0f, p.update(1300).x, eps)
    }

    @Test fun nonReturningStickHoldsPositionUntilReset() {
        val p = proc(JoystickConfig(returnToCenter = false))
        p.down(300f, 300f, 0, 300f, 300f); p.move(400f, 300f); p.update(16)
        p.up(32)
        assertEquals(1f, p.update(48).x, eps)
        p.reset()
        assertEquals(0f, p.update(64).x, eps)
    }

    @Test fun smoothModeSettlesToZeroAfterRelease() {
        val p = proc(JoystickConfig(mode = ResponseMode.Smooth))
        p.down(300f, 300f, 0, 300f, 300f); p.move(400f, 300f)
        var t = 16L
        p.update(t)
        p.up(t)
        var n = 0
        while (p.needsFrame() && n++ < 300) { t += 16; p.update(t) }
        assertFalse(p.needsFrame())
        assertEquals(0f, p.update(t + 16).x, eps)
    }

    @Test fun accelerationRampsOutputIn() {
        val p = proc(JoystickConfig(acceleration = 1f)) // 400 ms ramp
        p.down(300f, 300f, 0, 300f, 300f); p.move(400f, 300f)
        assertEquals(0.5f, p.update(200).x, eps)
        assertEquals(1f, p.update(400).x, eps)
    }

    @Test fun configJsonRoundTrips() {
        val c = JoystickConfig(sizeDp = 150f, mode = ResponseMode.Smooth, invertY = true, floating = true, vibration = true)
        assertEquals(c, JoystickConfig.fromJson(c.toJson()))
    }

    @Test fun invalidJsonValuesAreClamped() {
        val o = JoystickConfig().toJson().put("sensitivity", 99.0).put("mode", "bogus")
        val c = JoystickConfig.fromJson(o)
        assertEquals(JoystickLimits.sensitivity.endInclusive, c.sensitivity, eps)
        assertEquals(ResponseMode.Linear, c.mode)
    }

    @Test fun presetOperations() {
        var s = JoystickSettings()
        s = s.select("precision")
        assertFalse(s.modified)
        s = s.edit(s.working.copy(sensitivity = 1.1f))
        assertTrue(s.modified)
        s = s.saveAsNew("  Mine  ", id = "c_1")
        assertEquals("Mine", s.find("c_1")!!.name)
        assertFalse(s.modified)
        s = s.duplicate("c_1", newId = "c_2")
        assertEquals("Mine copy", s.find("c_2")!!.name)
        s = s.rename("c_2", "Other")
        assertEquals("Other", s.find("c_2")!!.name)
        // built-ins can't be renamed, overwritten or deleted
        assertEquals(s, s.rename("default", "X"))
        assertEquals(s, s.delete("default"))
        s = s.delete("c_2")
        assertEquals(JoystickPresets.DEFAULT_ID, s.activeId)
        assertEquals(JoystickConfig(), s.working)
        assertEquals(1, s.custom.size)
    }

    @Test fun settingsJsonRoundTripsWithCustomPresets() {
        val s = JoystickSettings().select("high").saveAsNew("Mine", id = "c_9")
            .edit(JoystickConfig(sizeDp = 170f))
        assertEquals(s, JoystickSettings.fromJson(s.toJson()))
    }

    @Test fun everyBuiltInPresetIsValidAndDistinct() {
        val configs = JoystickPresets.builtIn.map { it.config }
        configs.forEach { assertEquals(it, it.sanitized()) }
        assertEquals(configs.size, configs.toSet().size)
    }
}
