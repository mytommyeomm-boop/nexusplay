package com.nexusplay.launcher.runtime

/** Pure decision logic, kept free of Android types so it can be unit tested. */
object RuntimeAssessor {

    fun capabilities(d: RuntimeDetection) = RuntimeCapabilities(
        canOpenApp = d.installed && d.hasLauncher,
        canLaunchShortcut = d.installed && d.entryFound && d.entryExported && d.entryEnabled,
        acceptsExternalInput = false
    )

    /**
     * Never returns Compatible: that needs evidence from a confirmed launch, which the app
     * cannot observe yet (Android gives no result back from the runtime).
     */
    fun assess(
        d: RuntimeDetection,
        deviceSdk: Int,
        is64BitArm: Boolean,
        primaryAbi: String,
        hasVulkan: Boolean
    ): RuntimeAssessment {
        if (!d.installed) {
            return RuntimeAssessment(CompatStatus.RuntimeMissing, "Not installed on this device.", emptyList())
        }
        val caps = capabilities(d)
        val minSdk = d.minSdk
        val sdkOk = minSdk == null || deviceSdk >= minSdk

        val evidence = buildList {
            add(Evidence("Version", versionText(d), null))
            if (minSdk != null) add(Evidence("Needs Android", "API $minSdk (device: $deviceSdk)", sdkOk))
            add(Evidence("CPU", if (is64BitArm) "$primaryAbi, 64-bit ARM" else "$primaryAbi, not 64-bit ARM", null))
            add(Evidence("Vulkan", if (hasVulkan) "Reported" else "Not reported", null))
            add(
                Evidence(
                    "Game launch entry",
                    when {
                        !d.entryFound -> "Not found"
                        !d.entryEnabled -> "Disabled"
                        d.entryExported -> "Open to other apps"
                        else -> "Private to the runtime"
                    },
                    caps.canLaunchShortcut
                )
            )
        }

        return when {
            !sdkOk -> RuntimeAssessment(
                CompatStatus.Incompatible,
                "This runtime needs a newer Android version than this device runs.", evidence
            )
            caps.canLaunchShortcut -> RuntimeAssessment(
                CompatStatus.Unknown,
                "Installed and open to launches from other apps. No launch has been confirmed on this device yet.",
                evidence
            )
            caps.canOpenApp -> RuntimeAssessment(
                CompatStatus.Partial,
                "Installed, but it keeps its game screen private. NexusPlay can open the app; games must be started inside it.",
                evidence
            )
            else -> RuntimeAssessment(
                CompatStatus.Incompatible,
                "Installed, but it offers no way to be opened or launched from here.", evidence
            )
        }
    }

    private fun versionText(d: RuntimeDetection): String {
        val name = d.versionName ?: "unknown"
        return if (d.versionCode != null) "$name (${d.versionCode})" else name
    }
}

object ShortcutPath {
    /** Returns an error message, or null when the path is acceptable. */
    fun validate(raw: String): String? {
        val p = raw.trim()
        return when {
            p.isEmpty() -> "Enter the path of an exported .desktop file."
            !p.startsWith("/") -> "The path must be absolute and start with /."
            p.contains("..") -> "Paths containing .. are not accepted."
            !p.endsWith(".desktop", ignoreCase = true) -> "The file must end with .desktop."
            else -> null
        }
    }
}
