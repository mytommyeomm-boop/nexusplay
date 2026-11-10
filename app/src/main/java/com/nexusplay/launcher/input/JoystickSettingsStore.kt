package com.nexusplay.launcher.input

import android.content.Context
import android.util.AtomicFile
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import org.json.JSONObject
import java.io.File
import java.io.IOException

/** Persists settings as JSON. AtomicFile means a crash mid-write cannot corrupt the saved copy. */
object JoystickSettingsStore {
    private const val FILE_NAME = "joystick_settings.json"

    fun load(context: Context): JoystickSettings {
        val target = File(context.filesDir, FILE_NAME)
        if (!target.exists()) return JoystickSettings()
        return try {
            val text = String(AtomicFile(target).readFully(), Charsets.UTF_8)
            JoystickSettings.fromJson(JSONObject(text))
        } catch (e: Exception) {
            // Keep the unreadable file for inspection instead of silently overwriting it.
            runCatching { target.renameTo(File(context.filesDir, "$FILE_NAME.bad")) }
            JoystickSettings()
        }
    }

    fun save(context: Context, settings: JoystickSettings): Boolean {
        val file = AtomicFile(File(context.filesDir, FILE_NAME))
        val stream = try {
            file.startWrite()
        } catch (e: IOException) {
            return false
        }
        return try {
            stream.write(settings.toJson().toString(2).toByteArray(Charsets.UTF_8))
            file.finishWrite(stream)
            true
        } catch (e: IOException) {
            file.failWrite(stream)
            false
        }
    }
}

/**
 * Live settings for the whole app. [update] changes the in-memory value at once (so every
 * preview reacts immediately) and marks it dirty; [flush] writes it to disk.
 */
class JoystickSettingsHolder(
    private val appContext: Context,
    initial: JoystickSettings
) {
    var settings by mutableStateOf(initial)
        private set

    var lastSaveFailed by mutableStateOf(false)
        private set

    private var dirty = false
    private val lock = Any()

    fun update(transform: (JoystickSettings) -> JoystickSettings) {
        synchronized(lock) {
            settings = transform(settings).sanitized()
            dirty = true
        }
    }

    fun replace(new: JoystickSettings) = update { new }

    fun flush() {
        val snapshot: JoystickSettings
        synchronized(lock) {
            if (!dirty) return
            snapshot = settings
            dirty = false
        }
        val ok = JoystickSettingsStore.save(appContext, snapshot)
        if (!ok) synchronized(lock) { dirty = true }
        lastSaveFailed = !ok
    }
}

val LocalJoystickSettings = staticCompositionLocalOf<JoystickSettingsHolder> {
    error("JoystickSettingsHolder was not provided")
}
