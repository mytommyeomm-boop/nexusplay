package com.nexusplay.launcher.mascot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MascotLogicTest {

    @Test fun sameNoticeIsNotRepeatedInsideItsCooldown() {
        val gate = NoticeGate(defaultCooldownMs = 60_000)
        assertTrue(gate.allow("k", 1, now = 0, visiblePriority = null))
        assertFalse(gate.allow("k", 1, now = 30_000, visiblePriority = null))
        assertTrue(gate.allow("k", 1, now = 61_000, visiblePriority = null))
    }

    @Test fun differentNoticesDoNotBlockEachOther() {
        val gate = NoticeGate()
        assertTrue(gate.allow("a", 1, 0, null))
        assertTrue(gate.allow("b", 1, 1, null))
    }

    @Test fun lessUrgentNoticeNeverReplacesAMoreUrgentOne() {
        val gate = NoticeGate()
        assertFalse(gate.allow("tip", NoticeKind.Tip.priority, 0, visiblePriority = NoticeKind.Error.priority))
        assertTrue(gate.allow("err", NoticeKind.Error.priority, 0, visiblePriority = NoticeKind.Tip.priority))
    }

    @Test fun zeroCooldownAllowsAnExplicitRepeat() {
        val gate = NoticeGate()
        assertTrue(gate.allow("preview", 0, 0, null, cooldownMs = 0))
        assertTrue(gate.allow("preview", 0, 1, null, cooldownMs = 0))
    }

    @Test fun tutorialStepsWithoutAVisibleTargetAreSkipped() {
        val steps = Tutorials.runtimes.steps
        val onlyCard = stepsWithVisibleAnchors(steps, setOf(Anchor.RuntimeCard, Anchor.RuntimeSetup))
        assertEquals(listOf(Anchor.RuntimeCard, Anchor.RuntimeSetup), onlyCard.map { it.anchor })
        assertTrue(stepsWithVisibleAnchors(steps, emptySet()).isEmpty())
    }

    @Test fun stepsWithoutAnAnchorAlwaysShow() {
        val home = stepsWithVisibleAnchors(Tutorials.home.steps, emptySet())
        assertEquals(1, home.size)
        assertEquals(null, home.single().anchor)
    }

    @Test fun tutorialsAreWellFormed() {
        assertEquals(Tutorials.all.size, Tutorials.all.map { it.id }.toSet().size)
        assertEquals(Tutorials.all.size, Tutorials.all.map { it.route }.toSet().size)
        assertTrue(Tutorials.all.all { it.steps.isNotEmpty() })
        assertEquals(Tutorials.joystick, Tutorials.forRoute("settings/joystick"))
    }

    @Test fun everyNoticeKindHasAPositiveDuration() {
        NoticeKind.entries.forEach { assertTrue(it.seconds > 0) }
    }
}
