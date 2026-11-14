package com.nexusplay.launcher.mascot

import androidx.annotation.StringRes
import com.nexusplay.launcher.R
import com.nexusplay.launcher.core.UiText
import com.nexusplay.launcher.ui.navigation.JOYSTICK_ROUTE
import com.nexusplay.launcher.ui.navigation.ROUTE_HOME
import com.nexusplay.launcher.ui.navigation.ROUTE_IMPORT
import com.nexusplay.launcher.ui.navigation.ROUTE_SETTINGS
import com.nexusplay.launcher.ui.navigation.RUNTIMES_ROUTE

/** Notice kinds. The pose comes only from the event mapping, never from the kind itself. */
enum class NoticeKind(val event: MascotEvent, val priority: Int, val seconds: Int) {
    Success(MascotEvent.NoticeSuccess, 1, 5),
    Warning(MascotEvent.NoticeWarning, 2, 8),
    Error(MascotEvent.NoticeError, 3, 10),
    Loading(MascotEvent.NoticeLoading, 1, 6),
    Info(MascotEvent.NoticeInfo, 0, 6),
    MissingRuntime(MascotEvent.MissingRuntime, 2, 9),
    LaunchIssue(MascotEvent.LaunchFailed, 2, 9),
    GamepadSaved(MascotEvent.GamepadSaved, 1, 5),
    Tip(MascotEvent.TourSuggestion, 0, 9)
}

class NoticeAction(val label: UiText, val onClick: () -> Unit)

class MascotNotice(
    val key: String,
    val kind: NoticeKind,
    val title: UiText,
    val message: UiText,
    val action: NoticeAction? = null
)

/**
 * Keeps notices helpful rather than annoying: the same key is not shown again inside its cooldown,
 * and a less urgent notice never replaces a more urgent one that is still on screen.
 */
class NoticeGate(private val defaultCooldownMs: Long = 60_000L) {
    private val lastShown = HashMap<String, Long>()

    fun allow(key: String, priority: Int, now: Long, visiblePriority: Int?, cooldownMs: Long? = null): Boolean {
        val last = lastShown[key]
        if (last != null && now - last < (cooldownMs ?: defaultCooldownMs)) return false
        if (visiblePriority != null && visiblePriority > priority) return false
        lastShown[key] = now
        return true
    }
}

/** Stable ids of the screen elements a tutorial step can highlight. */
object Anchor {
    const val NavBar = "nav.bar"
    const val HomeHero = "home.hero"
    const val HomeModules = "home.modules"
    const val ImportIntake = "import.intake"
    const val ImportSteps = "import.steps"
    const val RuntimeCard = "runtime.card"
    const val RuntimeTest = "runtime.test"
    const val RuntimeSetup = "runtime.setup"
    const val JoystickPlate = "joystick.plate"
    const val JoystickCalibrate = "joystick.calibrate"
    const val JoystickSensitivity = "joystick.sensitivity"
    const val SettingsPrefs = "settings.prefs"
}

class TutorialStep(
    val anchor: String?,
    @StringRes val title: Int,
    @StringRes val body: Int,
    val event: MascotEvent
)

class Tutorial(val id: String, val route: String, @StringRes val nameRes: Int, val steps: List<TutorialStep>)

/** Only screens that exist today have a tutorial. New screens add an entry here plus strings. */
object Tutorials {
    val home = Tutorial(
        "home", ROUTE_HOME, R.string.tut_home_name, listOf(
            TutorialStep(Anchor.HomeHero, R.string.tut_home_1_title, R.string.tut_home_1_body, MascotEvent.TutorialExplain),
            TutorialStep(Anchor.HomeModules, R.string.tut_home_2_title, R.string.tut_home_2_body, MascotEvent.TutorialExplain),
            TutorialStep(Anchor.NavBar, R.string.tut_home_3_title, R.string.tut_home_3_body, MascotEvent.TutorialExplain),
            TutorialStep(null, R.string.tut_home_4_title, R.string.tut_home_4_body, MascotEvent.TutorialDone)
        )
    )
    val importing = Tutorial(
        "import", ROUTE_IMPORT, R.string.tut_import_name, listOf(
            TutorialStep(Anchor.ImportIntake, R.string.tut_import_1_title, R.string.tut_import_1_body, MascotEvent.TutorialExplain),
            TutorialStep(Anchor.ImportSteps, R.string.tut_import_2_title, R.string.tut_import_2_body, MascotEvent.TutorialExplain)
        )
    )
    val runtimes = Tutorial(
        "runtimes", RUNTIMES_ROUTE, R.string.tut_runtime_name, listOf(
            TutorialStep(Anchor.RuntimeCard, R.string.tut_runtime_1_title, R.string.tut_runtime_1_body, MascotEvent.TutorialExplain),
            TutorialStep(Anchor.RuntimeTest, R.string.tut_runtime_2_title, R.string.tut_runtime_2_body, MascotEvent.TutorialExplain),
            TutorialStep(Anchor.RuntimeSetup, R.string.tut_runtime_3_title, R.string.tut_runtime_3_body, MascotEvent.TutorialAdvice)
        )
    )
    val joystick = Tutorial(
        "joystick", JOYSTICK_ROUTE, R.string.tut_joystick_name, listOf(
            TutorialStep(Anchor.JoystickPlate, R.string.tut_joystick_1_title, R.string.tut_joystick_1_body, MascotEvent.TutorialGaming),
            TutorialStep(Anchor.JoystickCalibrate, R.string.tut_joystick_2_title, R.string.tut_joystick_2_body, MascotEvent.TutorialExplain),
            TutorialStep(Anchor.JoystickSensitivity, R.string.tut_joystick_3_title, R.string.tut_joystick_3_body, MascotEvent.TutorialExplain)
        )
    )
    val settings = Tutorial(
        "settings", ROUTE_SETTINGS, R.string.tut_settings_name, listOf(
            TutorialStep(Anchor.SettingsPrefs, R.string.tut_settings_1_title, R.string.tut_settings_1_body, MascotEvent.TutorialExplain)
        )
    )

    val all = listOf(home, importing, runtimes, joystick, settings)
    fun forRoute(route: String?): Tutorial? = all.firstOrNull { it.route == route }
}

/** A step is only relevant when the thing it points at is actually on screen right now. */
fun stepsWithVisibleAnchors(steps: List<TutorialStep>, visibleAnchors: Set<String>): List<TutorialStep> =
    steps.filter { it.anchor == null || it.anchor in visibleAnchors }
