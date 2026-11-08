package com.nexusplay.launcher.input

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONObject

/**
 * Loads and saves [JoystickSettings] in SharedPreferences (no extra dependency). Every change is
 * written immediately. A second copy at least a minute old is kept as a fallback, and an
 * unreadable primary copy falls back to it instead of silently resetting the user's presets.
 */
class JoystickRepository private constructor(context: Context) {
    private val prefs = context.getSharedPreferences("joystick_settings", Context.MODE_PRIVATE)

    var settings: JoystickSettings by mutableStateOf(load())
        private set

    fun update(transform: (JoystickSettings) -> JoystickSettings) {
        val next = transform(settings)
        if (next == settings) return
        settings = next
        persist(next)
    }

    private fun load(): JoystickSettings {
        for (key in listOf(KEY, KEY_PREV)) {
            val text = prefs.getString(key, null) ?: continue
            runCatching { return JoystickSettings.fromJson(JSONObject(text)) }
        }
        return JoystickSettings()
    }

    private fun persist(s: JoystickSettings) {
        val now = System.currentTimeMillis()
        val current = prefs.getString(KEY, null)
        val editor = prefs.edit()
        if (current != null && now - prefs.getLong(KEY_PREV_AT, 0L) > BACKUP_INTERVAL_MS) {
            editor.putString(KEY_PREV, current).putLong(KEY_PREV_AT, now)
        }
        editor.putString(KEY, s.toJson().toString()).apply()
    }

    companion object {
        private const val KEY = "state_v1"
        private const val KEY_PREV = "state_v1_prev"
        private const val KEY_PREV_AT = "state_v1_prev_at"
        private const val BACKUP_INTERVAL_MS = 60_000L

        @Volatile private var instance: JoystickRepository? = null

        fun get(context: Context): JoystickRepository =
            instance ?: synchronized(this) {
                instance ?: JoystickRepository(context.applicationContext).also { instance = it }
            }
    }
}
