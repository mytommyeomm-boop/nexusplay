package com.nexusplay.launcher.mascot

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.nexusplay.launcher.R
import com.nexusplay.launcher.core.AppLanguage
import com.nexusplay.launcher.core.UiText
import com.nexusplay.launcher.ui.navigation.JOYSTICK_ROUTE
import com.nexusplay.launcher.ui.navigation.ROUTE_HOME
import com.nexusplay.launcher.ui.navigation.ROUTE_IMPORT
import com.nexusplay.launcher.ui.navigation.ROUTE_SETTINGS
import com.nexusplay.launcher.ui.navigation.RUNTIMES_ROUTE

/** Poses cut from the supplied sprite sheets (res/drawable-nodpi/mascot_*.webp, transparent). */
enum class MascotPose(@DrawableRes val res: Int, @StringRes val nameRes: Int) {
    Wave(R.drawable.mascot_wave, R.string.pose_wave),
    Talking(R.drawable.mascot_talking, R.string.pose_talking),
    Silent(R.drawable.mascot_silent, R.string.pose_silent),
    Happy(R.drawable.mascot_happy, R.string.pose_happy),
    Confused(R.drawable.mascot_confused, R.string.pose_confused),
    Surprised(R.drawable.mascot_surprised, R.string.pose_surprised),
    Sleepy(R.drawable.mascot_sleepy, R.string.pose_sleepy),
    Sleeping(R.drawable.mascot_sleeping, R.string.pose_sleeping),
    Dancing(R.drawable.mascot_dancing, R.string.pose_dancing),
    Thinking(R.drawable.mascot_thinking, R.string.pose_thinking),
    Sad(R.drawable.mascot_sad, R.string.pose_sad),
    Excited(R.drawable.mascot_excited, R.string.pose_excited),
    Gaming(R.drawable.mascot_gaming, R.string.pose_gaming),
    Loading(R.drawable.mascot_loading, R.string.pose_loading),
    Announce(R.drawable.mascot_announce, R.string.pose_announce),
    Dizzy(R.drawable.mascot_dizzy, R.string.pose_dizzy),
    Thanks(R.drawable.mascot_thanks_th, R.string.pose_thanks);

    /** The Thanks art holds a Thai sign, so every other language gets the Happy art instead. */
    @DrawableRes
    fun drawableFor(language: AppLanguage?): Int =
        if (this == Thanks && language != AppLanguage.Thai) R.drawable.mascot_happy else res
}

/** Which pose, how urgent and how long (seconds) each kind of notice is shown. */
enum class NoticeKind(val pose: MascotPose, val priority: Int, val seconds: Int) {
    Success(MascotPose.Thanks, 1, 5),
    Warning(MascotPose.Surprised, 2, 8),
    Error(MascotPose.Sad, 3, 10),
    Loading(MascotPose.Loading, 1, 6),
    Info(MascotPose.Announce, 0, 6),
    MissingRuntime(MascotPose.Confused, 2, 9),
    LaunchIssue(MascotPose.Dizzy, 2, 9),
    GamepadSaved(MascotPose.Gaming, 1, 5),
    Tip(MascotPose.Talking, 0, 9)
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
    val pose: MascotPose
)

class Tutorial(val id: String, val route: String, @StringRes val nameRes: Int, val steps: List<TutorialStep>)

/** Only screens that exist today have a tutorial. New screens add an entry here plus strings. */
object Tutorials {
    val home = Tutorial(
        "home", ROUTE_HOME, R.string.tut_home_name, listOf(
            TutorialStep(Anchor.HomeHero, R.string.tut_home_1_title, R.string.tut_home_1_body, MascotPose.Talking),
            TutorialStep(Anchor.HomeModules, R.string.tut_home_2_title, R.string.tut_home_2_body, MascotPose.Happy),
            TutorialStep(Anchor.NavBar, R.string.tut_home_3_title, R.string.tut_home_3_body, MascotPose.Talking),
            TutorialStep(null, R.string.tut_home_4_title, R.string.tut_home_4_body, MascotPose.Excited)
        )
    )
    val importing = Tutorial(
        "import", ROUTE_IMPORT, R.string.tut_import_name, listOf(
            TutorialStep(Anchor.ImportIntake, R.string.tut_import_1_title, R.string.tut_import_1_body, MascotPose.Thinking),
            TutorialStep(Anchor.ImportSteps, R.string.tut_import_2_title, R.string.tut_import_2_body, MascotPose.Talking)
        )
    )
    val runtimes = Tutorial(
        "runtimes", RUNTIMES_ROUTE, R.string.tut_runtime_name, listOf(
            TutorialStep(Anchor.RuntimeCard, R.string.tut_runtime_1_title, R.string.tut_runtime_1_body, MascotPose.Thinking),
            TutorialStep(Anchor.RuntimeTest, R.string.tut_runtime_2_title, R.string.tut_runtime_2_body, MascotPose.Talking),
            TutorialStep(Anchor.RuntimeSetup, R.string.tut_runtime_3_title, R.string.tut_runtime_3_body, MascotPose.Happy)
        )
    )
    val joystick = Tutorial(
        "joystick", JOYSTICK_ROUTE, R.string.tut_joystick_name, listOf(
            TutorialStep(Anchor.JoystickPlate, R.string.tut_joystick_1_title, R.string.tut_joystick_1_body, MascotPose.Gaming),
            TutorialStep(Anchor.JoystickCalibrate, R.string.tut_joystick_2_title, R.string.tut_joystick_2_body, MascotPose.Talking),
            TutorialStep(Anchor.JoystickSensitivity, R.string.tut_joystick_3_title, R.string.tut_joystick_3_body, MascotPose.Silent)
        )
    )
    val settings = Tutorial(
        "settings", ROUTE_SETTINGS, R.string.tut_settings_name, listOf(
            TutorialStep(Anchor.SettingsPrefs, R.string.tut_settings_1_title, R.string.tut_settings_1_body, MascotPose.Happy)
        )
    )

    val all = listOf(home, importing, runtimes, joystick, settings)
    fun forRoute(route: String?): Tutorial? = all.firstOrNull { it.route == route }
}

/** A step is only relevant when the thing it points at is actually on screen right now. */
fun stepsWithVisibleAnchors(steps: List<TutorialStep>, visibleAnchors: Set<String>): List<TutorialStep> =
    steps.filter { it.anchor == null || it.anchor in visibleAnchors }
