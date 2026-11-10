package com.nexusplay.launcher.runtime

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.nexusplay.launcher.R
import com.nexusplay.launcher.core.UiText

/** Only runtimes whose package and launch entry were checked against public sources are listed. */
object RuntimeRegistry {
    val backends: List<RuntimeBackend> = listOf(
        WinlatorBackend(
            id = "winlator-cmod",
            displayName = "Winlator Cmod",
            packageName = "com.winlator.cmod",
            entryClass = "com.winlator.cmod.XServerDisplayActivity",
            projectUrl = "https://github.com/coffincolors/winlator/releases",
            setupSteps = listOf(
                UiText(R.string.cmod_step_1), UiText(R.string.cmod_step_2), UiText(R.string.cmod_step_3),
                UiText(R.string.cmod_step_4), UiText(R.string.cmod_step_5)
            ),
            knownLimits = listOf(
                UiText(R.string.cmod_limit_1), UiText(R.string.cmod_limit_2),
                UiText(R.string.cmod_limit_3), UiText(R.string.cmod_limit_4)
            )
        ),
        WinlatorBackend(
            id = "winlator",
            displayName = "Winlator (com.winlator)",
            packageName = "com.winlator",
            entryClass = "com.winlator.XServerDisplayActivity",
            projectUrl = "https://github.com/brunodev85/winlator/releases",
            setupSteps = listOf(
                UiText(R.string.official_step_1), UiText(R.string.official_step_2),
                UiText(R.string.official_step_3), UiText(R.string.official_step_4)
            ),
            knownLimits = listOf(
                UiText(R.string.official_limit_1), UiText(R.string.official_limit_2), UiText(R.string.official_limit_3)
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
