package com.nexusplay.launcher.runtime

import android.content.Context

enum class CompatStatus { Compatible, Partial, Unknown, Incompatible, RuntimeMissing }

/** One fact shown to the user. [ok] is true/false when it is a pass/fail check, null when informational. */
data class Evidence(val label: String, val value: String, val ok: Boolean?)

data class RuntimeDetection(
    val installed: Boolean,
    val versionName: String?,
    val versionCode: Long?,
    val minSdk: Int?,
    val hasLauncher: Boolean,
    val entryFound: Boolean,
    val entryExported: Boolean,
    val entryEnabled: Boolean
) {
    companion object {
        val Missing = RuntimeDetection(false, null, null, null, false, false, false, false)
    }
}

data class RuntimeCapabilities(
    val canOpenApp: Boolean,
    val canLaunchShortcut: Boolean,
    /** No verified way exists to feed external input into any supported runtime, so this is false. */
    val acceptsExternalInput: Boolean
)

data class RuntimeAssessment(val status: CompatStatus, val summary: String, val evidence: List<Evidence>)

sealed interface LaunchResult {
    /** Android accepted the intent. This does NOT prove a game is running. */
    data class HandedOver(val detail: String) : LaunchResult
    data class Failed(val reason: String) : LaunchResult
}

/** A compatibility runtime NexusPlay can detect and hand games to. */
interface RuntimeBackend {
    val id: String
    val displayName: String
    val packageName: String
    val projectUrl: String
    val setupSteps: List<String>
    val knownLimits: List<String>

    fun detect(context: Context): RuntimeDetection
    fun openApp(context: Context): LaunchResult
    fun launchShortcut(context: Context, shortcutPath: String): LaunchResult
}
