package com.nexusplay.launcher.runtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RuntimeAssessorTest {
    private fun installed(exported: Boolean, found: Boolean = true, launcher: Boolean = true, minSdk: Int? = 26) =
        RuntimeDetection(true, "1.0", 10L, minSdk, launcher, found, exported, found)

    private fun assess(d: RuntimeDetection, sdk: Int = 34) =
        RuntimeAssessor.assess(d, sdk, true, "arm64-v8a", true)

    @Test fun missingRuntime() {
        assertEquals(CompatStatus.RuntimeMissing, assess(RuntimeDetection.Missing).status)
    }

    @Test fun exportedEntryIsUnknownNeverCompatible() {
        val a = assess(installed(exported = true))
        assertEquals(CompatStatus.Unknown, a.status)
        assertTrue(RuntimeAssessor.capabilities(installed(true)).canLaunchShortcut)
    }

    @Test fun privateEntryIsPartial() {
        val d = installed(exported = false)
        assertEquals(CompatStatus.Partial, assess(d).status)
        assertFalse(RuntimeAssessor.capabilities(d).canLaunchShortcut)
        assertTrue(RuntimeAssessor.capabilities(d).canOpenApp)
    }

    @Test fun newerMinSdkThanDeviceIsIncompatible() {
        assertEquals(CompatStatus.Incompatible, assess(installed(true, minSdk = 35), sdk = 33).status)
    }

    @Test fun nothingToOpenOrLaunchIsIncompatible() {
        assertEquals(CompatStatus.Incompatible, assess(installed(false, found = false, launcher = false)).status)
    }

    @Test fun externalInputIsNeverClaimed() {
        assertFalse(RuntimeAssessor.capabilities(installed(true)).acceptsExternalInput)
    }

    @Test fun shortcutPathValidation() {
        assertNull(ShortcutPath.validate("/storage/emulated/0/Winlator/game.desktop"))
        assertNotNull(ShortcutPath.validate(""))
        assertNotNull(ShortcutPath.validate("game.desktop"))
        assertNotNull(ShortcutPath.validate("/sdcard/../x.desktop"))
        assertNotNull(ShortcutPath.validate("/sdcard/game.exe"))
    }
}
