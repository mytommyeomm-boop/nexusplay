package com.nexusplay.launcher.input

import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject

data class JoystickPreset(
    val id: String,
    val name: String,
    val config: JoystickConfig,
    val builtIn: Boolean
)

object JoystickPresets {
    const val DEFAULT_ID = "default"

    val builtIn: List<JoystickPreset> = listOf(
        JoystickPreset(DEFAULT_ID, "Default", JoystickConfig(), true),
        JoystickPreset(
            "balanced", "Balanced",
            JoystickConfig(
                mode = ResponseMode.Curve, curve = 1.3f, innerDeadzone = 0.10f, outerDeadzone = 0.05f,
                returnSpeed = 0.8f
            ), true
        ),
        JoystickPreset(
            "high", "High Sensitivity",
            JoystickConfig(
                sizeDp = 150f, sensitivity = 1.6f, range = 0.85f, innerDeadzone = 0.05f,
                outerDeadzone = 0.05f, mode = ResponseMode.Fast
            ), true
        ),
        JoystickPreset(
            "precision", "Precision",
            JoystickConfig(
                sizeDp = 120f, sensitivity = 0.7f, range = 1.2f, innerDeadzone = 0.12f,
                outerDeadzone = 0.02f, mode = ResponseMode.Precision, returnSpeed = 0.6f
            ), true
        ),
        JoystickPreset(
            "smooth", "Smooth",
            JoystickConfig(
                mode = ResponseMode.Smooth, curve = 1.4f, acceleration = 0.35f, returnSpeed = 0.5f,
                innerDeadzone = 0.08f
            ), true
        )
    )
}

/**
 * Immutable settings model. Every operation returns a new instance, which keeps the logic
 * trivially testable; [JoystickRepository] persists whatever the latest instance is.
 * "Custom" in the UI means [modified]: the working config differs from the selected preset.
 */
data class JoystickSettings(
    val activeId: String = JoystickPresets.DEFAULT_ID,
    val working: JoystickConfig = JoystickConfig(),
    val custom: List<JoystickPreset> = emptyList()
) {
    val all: List<JoystickPreset> get() = JoystickPresets.builtIn + custom
    fun find(id: String): JoystickPreset? = all.firstOrNull { it.id == id }
    val activePreset: JoystickPreset get() = find(activeId) ?: JoystickPresets.builtIn.first()
    val modified: Boolean get() = working != activePreset.config

    fun select(id: String): JoystickSettings {
        val p = find(id) ?: return this
        return copy(activeId = p.id, working = p.config)
    }

    fun edit(config: JoystickConfig): JoystickSettings = copy(working = config.sanitized())

    /** Discards unsaved edits and returns to the selected preset's stored values. */
    fun revert(): JoystickSettings = copy(working = activePreset.config)

    fun restoreDefaults(): JoystickSettings = select(JoystickPresets.DEFAULT_ID)

    fun saveAsNew(name: String, id: String = newId()): JoystickSettings {
        val preset = JoystickPreset(id, cleanName(name), working, false)
        return copy(custom = custom + preset, activeId = id)
    }

    fun overwriteActive(): JoystickSettings {
        if (activePreset.builtIn) return this
        return copy(custom = custom.map { if (it.id == activeId) it.copy(config = working) else it })
    }

    fun rename(id: String, name: String): JoystickSettings =
        copy(custom = custom.map { if (it.id == id) it.copy(name = cleanName(name)) else it })

    fun duplicate(id: String, newId: String = newId()): JoystickSettings {
        val p = find(id) ?: return this
        val dup = JoystickPreset(newId, cleanName(p.name + " copy"), p.config, false)
        return copy(custom = custom + dup, activeId = newId, working = p.config)
    }

    fun delete(id: String): JoystickSettings {
        if (custom.none { it.id == id }) return this
        val next = copy(custom = custom.filterNot { it.id == id })
        return if (activeId == id) next.restoreDefaults() else next
    }

    fun toJson(): JSONObject = JSONObject().apply {
        put("version", 1)
        put("activeId", activeId)
        put("working", working.toJson())
        put("custom", JSONArray().apply {
            custom.forEach {
                put(JSONObject().put("id", it.id).put("name", it.name).put("config", it.config.toJson()))
            }
        })
    }

    companion object {
        private fun newId() = "c_" + UUID.randomUUID().toString().take(12)
        private fun cleanName(n: String) = n.trim().take(24).ifBlank { "Custom" }

        fun fromJson(o: JSONObject): JoystickSettings {
            val arr = o.optJSONArray("custom") ?: JSONArray()
            val custom = (0 until arr.length()).mapNotNull { i ->
                val p = arr.optJSONObject(i) ?: return@mapNotNull null
                val id = p.optString("id")
                if (id.isBlank() || JoystickPresets.builtIn.any { it.id == id }) return@mapNotNull null
                JoystickPreset(id, cleanName(p.optString("name")), JoystickConfig.fromJson(p.optJSONObject("config") ?: JSONObject()), false)
            }
            val base = JoystickSettings(custom = custom)
            val activeId = o.optString("activeId").takeIf { id -> base.find(id) != null } ?: JoystickPresets.DEFAULT_ID
            val working = o.optJSONObject("working")?.let { JoystickConfig.fromJson(it) } ?: base.find(activeId)!!.config
            return base.copy(activeId = activeId, working = working)
        }
    }
}
