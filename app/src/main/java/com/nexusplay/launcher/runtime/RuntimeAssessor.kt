package com.nexusplay.launcher.runtime

import com.nexusplay.launcher.R
import com.nexusplay.launcher.core.UiText

/** Pure decision logic with no Android services, so it can be unit tested. Messages are UiText (Thai at display time). */
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
            return RuntimeAssessment(CompatStatus.RuntimeMissing, UiText(R.string.rt_sum_missing), emptyList())
        }
        val caps = capabilities(d)
        val minSdk = d.minSdk
        val sdkOk = minSdk == null || deviceSdk >= minSdk

        val evidence = buildList {
            add(
                Evidence(
                    UiText(R.string.rt_ev_version),
                    UiText(
                        R.string.rt_ev_version_fmt,
                        d.versionName ?: UiText(R.string.unknown),
                        d.versionCode?.toString() ?: "?"
                    ),
                    null
                )
            )
            if (minSdk != null) {
                add(Evidence(UiText(R.string.rt_ev_needs_android), UiText(R.string.rt_ev_needs_value, minSdk, deviceSdk), sdkOk))
            }
            add(
                Evidence(
                    UiText(R.string.rt_ev_cpu),
                    UiText(if (is64BitArm) R.string.rt_ev_cpu_arm64 else R.string.rt_ev_cpu_other, primaryAbi),
                    null
                )
            )
            add(
                Evidence(
                    UiText(R.string.vulkan_label),
                    UiText(if (hasVulkan) R.string.settings_vulkan_yes else R.string.settings_vulkan_no),
                    null
                )
            )
            add(
                Evidence(
                    UiText(R.string.rt_ev_entry),
                    UiText(
                        when {
                            !d.entryFound -> R.string.rt_entry_missing
                            !d.entryEnabled -> R.string.rt_entry_disabled
                            d.entryExported -> R.string.rt_entry_open
                            else -> R.string.rt_entry_private
                        }
                    ),
                    caps.canLaunchShortcut
                )
            )
        }

        return when {
            !sdkOk -> RuntimeAssessment(CompatStatus.Incompatible, UiText(R.string.rt_sum_old_android), evidence)
            caps.canLaunchShortcut -> RuntimeAssessment(CompatStatus.Unknown, UiText(R.string.rt_sum_unknown), evidence)
            caps.canOpenApp -> RuntimeAssessment(CompatStatus.Partial, UiText(R.string.rt_sum_partial), evidence)
            else -> RuntimeAssessment(CompatStatus.Incompatible, UiText(R.string.rt_sum_no_entry), evidence)
        }
    }
}

object ShortcutPath {
    /** Returns a Thai-resource error message, or null when the path is acceptable. */
    fun validate(raw: String): UiText? {
        val p = raw.trim()
        return when {
            p.isEmpty() -> UiText(R.string.path_err_empty)
            !p.startsWith("/") -> UiText(R.string.path_err_relative)
            p.contains("..") -> UiText(R.string.path_err_dots)
            !p.endsWith(".desktop", ignoreCase = true) -> UiText(R.string.path_err_extension)
            else -> null
        }
    }
}
