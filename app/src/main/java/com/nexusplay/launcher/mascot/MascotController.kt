package com.nexusplay.launcher.mascot

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Rect
import com.nexusplay.launcher.R
import com.nexusplay.launcher.core.UiText

val LocalMascot = staticCompositionLocalOf<MascotController> { error("MascotController was not provided") }

/**
 * One owner for everything the mascot does: preferences (persisted), the welcome flag, notices
 * and tutorials. All values are Compose state, so screens react immediately.
 */
class MascotController(private val prefs: SharedPreferences) {

    // ---- preferences (saved as soon as they change)
    var enabled by mutableStateOf(prefs.getBoolean(K_ENABLED, true)); private set
    var animations by mutableStateOf(prefs.getBoolean(K_ANIM, true)); private set
    var tutorialSuggestions by mutableStateOf(prefs.getBoolean(K_TIPS, true)); private set
    var notifications by mutableStateOf(prefs.getBoolean(K_NOTICES, true)); private set
    var sizeLevel by mutableIntStateOf(prefs.getInt(K_SIZE, 1).coerceIn(0, 2)); private set

    val sizeFactor: Float get() = when (sizeLevel) { 0 -> 0.8f; 2 -> 1.25f; else -> 1f }

    fun updateEnabled(v: Boolean) { enabled = v; prefs.edit().putBoolean(K_ENABLED, v).apply() }
    fun updateAnimations(v: Boolean) { animations = v; prefs.edit().putBoolean(K_ANIM, v).apply() }
    fun updateTutorialSuggestions(v: Boolean) { tutorialSuggestions = v; prefs.edit().putBoolean(K_TIPS, v).apply() }
    fun updateNotifications(v: Boolean) { notifications = v; prefs.edit().putBoolean(K_NOTICES, v).apply() }
    fun updateSizeLevel(v: Int) { sizeLevel = v.coerceIn(0, 2); prefs.edit().putInt(K_SIZE, sizeLevel).apply() }

    // ---- welcome (shown once; can be replayed from Settings)
    var welcomeVisible by mutableStateOf(!prefs.getBoolean(K_ONBOARD, false)); private set

    fun completeWelcome(startTour: Boolean) {
        welcomeVisible = false
        prefs.edit().putBoolean(K_ONBOARD, true).apply()
        if (startTour) requestTutorial(Tutorials.home)
    }

    fun showWelcomeAgain() { welcomeVisible = true }

    // ---- notices
    var notice by mutableStateOf<MascotNotice?>(null); private set
    private val gate = NoticeGate()

    /** [force] skips the notifications switch for things the user explicitly asked for (tours, previews). */
    fun notify(n: MascotNotice, cooldownMs: Long? = null, force: Boolean = false): Boolean {
        if (!notifications && !force) return false
        if (!gate.allow(n.key, n.kind.priority, System.currentTimeMillis(), notice?.kind?.priority, cooldownMs)) return false
        notice = n
        return true
    }

    fun dismissNotice(n: MascotNotice) { if (notice === n) notice = null }

    // ---- tutorials
    private var completed by mutableStateOf(readSet(K_DONE))
    private var suggested by mutableStateOf(readSet(K_SUGGESTED))
    val anchors = mutableStateMapOf<String, Rect>()

    var pendingTutorial by mutableStateOf<Tutorial?>(null); private set
    var active by mutableStateOf<Tutorial?>(null); private set
    var steps by mutableStateOf<List<TutorialStep>>(emptyList()); private set
    var stepIndex by mutableIntStateOf(0); private set
    var resolving by mutableStateOf(false); private set

    fun isCompleted(id: String) = id in completed

    /** Asks for a tutorial; the navigation host moves to its screen and then calls [beginPending]. */
    fun requestTutorial(t: Tutorial) { notice = null; pendingTutorial = t }

    fun beginPending() {
        val t = pendingTutorial ?: return
        pendingTutorial = null
        active = t; steps = emptyList(); stepIndex = 0; resolving = true
    }

    /** Called once the screen has laid out: keep only steps whose target is really visible. */
    fun resolveSteps(windowHeight: Float) {
        val t = active ?: return
        val visible = anchors.filter { (_, r) -> r.height > 0f && r.bottom > 0f && r.top < windowHeight }.keys
        steps = stepsWithVisibleAnchors(t.steps, visible)
        resolving = false
        if (steps.isEmpty()) finish()
    }

    fun next() { if (stepIndex < steps.lastIndex) stepIndex++ else finish() }
    fun back() { if (stepIndex > 0) stepIndex-- }
    fun skip() = finish()

    private fun finish() {
        active?.let { completed = completed + it.id; saveSet(K_DONE, completed) }
        active = null; steps = emptyList(); stepIndex = 0; resolving = false
    }

    fun resetTutorials() {
        completed = emptySet(); suggested = emptySet()
        saveSet(K_DONE, completed); saveSet(K_SUGGESTED, suggested)
        active = null; pendingTutorial = null
    }

    /** Offers a tour once per screen, never while something else is going on. */
    fun suggestFor(route: String?) {
        val t = Tutorials.forRoute(route) ?: return
        if (!tutorialSuggestions || welcomeVisible || active != null || pendingTutorial != null) return
        if (t.id in completed || t.id in suggested) return
        val posted = notify(
            MascotNotice(
                key = "tour_${t.id}",
                kind = NoticeKind.Tip,
                title = UiText(R.string.tip_suggest_title),
                message = UiText(R.string.tip_suggest_message),
                action = NoticeAction(UiText(R.string.tip_suggest_action)) { requestTutorial(t) }
            ),
            cooldownMs = 0L, force = true
        )
        if (posted) { suggested = suggested + t.id; saveSet(K_SUGGESTED, suggested) }
    }

    private fun readSet(key: String): Set<String> = prefs.getStringSet(key, emptySet())?.toSet() ?: emptySet()
    private fun saveSet(key: String, value: Set<String>) { prefs.edit().putStringSet(key, value).apply() }

    private companion object {
        const val K_ENABLED = "enabled"
        const val K_ANIM = "animations"
        const val K_TIPS = "tutorial_suggestions"
        const val K_NOTICES = "notifications"
        const val K_SIZE = "size"
        const val K_ONBOARD = "onboarding_done"
        const val K_DONE = "tutorials_done"
        const val K_SUGGESTED = "tutorials_suggested"
    }
}
