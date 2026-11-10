package com.nexusplay.launcher.runtime

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Only runtimes whose package and launch entry were checked against public sources are listed. */
object RuntimeRegistry {
    val backends: List<RuntimeBackend> = listOf(
        WinlatorBackend(
            id = "winlator-cmod",
            displayName = "Winlator Cmod",
            packageName = "com.winlator.cmod",
            entryClass = "com.winlator.cmod.XServerDisplayActivity",
            projectUrl = "https://github.com/coffincolors/winlator",
            setupSteps = listOf(
                "Download the APK from the Releases page of the Winlator Cmod project and install it.",
                "Open Winlator once and wait for its first-time setup to finish.",
                "Create a container, then create a shortcut for your game inside Winlator.",
                "Use Winlator's frontend export on that shortcut to save a .desktop file to shared storage.",
                "Come back here, paste that file's path into the test field and launch it."
            ),
            knownLimits = listOf(
                "NexusPlay cannot create shortcuts. They must be made inside Winlator first.",
                "Android returns no result from the runtime, so NexusPlay cannot tell whether the game started or when it exits.",
                "Cmod has its own touch controls and controller support. NexusPlay's virtual gamepad cannot send input into it.",
                "The launch method comes from public sources and has not been confirmed on your device. The test field is how you confirm it."
            )
        ),
        WinlatorBackend(
            id = "winlator",
            displayName = "Winlator (com.winlator)",
            packageName = "com.winlator",
            entryClass = "com.winlator.XServerDisplayActivity",
            projectUrl = "https://github.com/brunodev85/winlator",
            setupSteps = listOf(
                "Download the APK from the Releases page of the Winlator project and install it.",
                "Open Winlator once and wait for its first-time setup to finish.",
                "Create a container and add your game as a shortcut inside Winlator.",
                "Start the game from Winlator's home screen shortcut."
            ),
            knownLimits = listOf(
                "Official Winlator keeps its game screen private, so NexusPlay can open the app but cannot start a game in it.",
                "Some older Winlator Cmod builds also use the package name com.winlator. Whether a build accepts launches is read from your device, not assumed.",
                "NexusPlay cannot send controller input into Winlator."
            )
        )
    )

    fun byId(id: String?): RuntimeBackend? = backends.firstOrNull { it.id == id }
}

/** The user's chosen runtime, remembered across restarts. */
object RuntimeSelection {
    private const val PREFS = "runtime_prefs"
    private const val KEY = "selected_runtime"

    var selectedId by mutableStateOf<String?>(null)
        private set

    fun load(context: Context) {
        selectedId = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)
    }

    fun select(context: Context, id: String?) {
        selectedId = id
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().apply {
            if (id == null) remove(KEY) else putString(KEY, id)
        }.apply()
    }
}
