package com.nexusplay.launcher.mascot

import com.nexusplay.launcher.core.AppLanguage
import org.json.JSONObject

enum class MouthRule { Any, Open, NotOpen }

/**
 * What the mascot is expressing. [requires] says which asset categories may be used for it, and
 * [mouthRule] adds a body-language check (talking needs an open mouth; listening and sleeping must not).
 * A pose is never used for an expression just because it looks similar.
 */
enum class MascotExpression(val requires: Set<MascotCategory>, val mouthRule: MouthRule = MouthRule.Any) {
    Neutral(setOf(MascotCategory.Listening), MouthRule.NotOpen),
    Listening(setOf(MascotCategory.Listening), MouthRule.NotOpen),
    Idle(setOf(MascotCategory.Idle)),
    Talking(setOf(MascotCategory.Talking), MouthRule.Open),
    Greeting(setOf(MascotCategory.Welcome)),
    Happy(setOf(MascotCategory.Happy)),
    Excited(setOf(MascotCategory.Excited)),
    Dancing(setOf(MascotCategory.Dancing)),
    Sad(setOf(MascotCategory.Sad)),
    Angry(setOf(MascotCategory.Angry)),
    Confused(setOf(MascotCategory.Confused)),
    Thinking(setOf(MascotCategory.Thinking)),
    Surprised(setOf(MascotCategory.Surprised)),
    Sleepy(setOf(MascotCategory.Sleepy), MouthRule.NotOpen),
    Sleeping(setOf(MascotCategory.Sleeping), MouthRule.NotOpen),
    Gaming(setOf(MascotCategory.Gaming)),
    Loading(setOf(MascotCategory.Loading)),
    Thanks(setOf(MascotCategory.Notifications, MascotCategory.Happy)),
    Cheer(setOf(MascotCategory.Cheer, MascotCategory.Excited));

    fun accepts(asset: MascotAsset): Boolean =
        asset.categories.any { it in requires } && when (mouthRule) {
            MouthRule.Any -> true
            MouthRule.Open -> asset.mouth == Mouth.Open
            MouthRule.NotOpen -> asset.mouth != Mouth.Open
        }
}

/**
 * Things that happen in the app. Each event has a default expression; [wired] says whether the app
 * triggers it today (the developer lab lists the rest as "not wired yet" instead of pretending).
 * Angry is reserved for playful pouting and is never used for normal errors.
 */
enum class MascotEvent(val expression: MascotExpression, val wired: Boolean) {
    Welcome(MascotExpression.Greeting, true),
    TutorialExplain(MascotExpression.Talking, true),
    TutorialAdvice(MascotExpression.Thinking, true),
    TutorialGaming(MascotExpression.Gaming, true),
    TutorialDone(MascotExpression.Happy, true),
    TourSuggestion(MascotExpression.Talking, true),
    NoticeSuccess(MascotExpression.Happy, true),
    NoticeInfo(MascotExpression.Talking, true),
    NoticeWarning(MascotExpression.Surprised, true),
    NoticeError(MascotExpression.Sad, true),
    NoticeLoading(MascotExpression.Loading, false),
    MissingRuntime(MascotExpression.Confused, true),
    LaunchFailed(MascotExpression.Sad, true),
    LaunchHandedOver(MascotExpression.Excited, false),
    GamepadSaved(MascotExpression.Happy, true),
    GamepadConfigured(MascotExpression.Dancing, false),
    GameImported(MascotExpression.Happy, false),
    FirstGameAdded(MascotExpression.Dancing, false),
    Achievement(MascotExpression.Dancing, false),
    DailyTip(MascotExpression.Thinking, false),
    Suggestion(MascotExpression.Thinking, false),
    QuickHelp(MascotExpression.Talking, false),
    SettingsOpened(MascotExpression.Talking, false),
    GamingMode(MascotExpression.Gaming, false),
    Idle(MascotExpression.Idle, false),
    Listening(MascotExpression.Listening, false),
    Sleep(MascotExpression.Sleeping, false),
    Poked(MascotExpression.Surprised, false),
    PokedRepeatedly(MascotExpression.Angry, false),
    Thanks(MascotExpression.Thanks, false),
    Encourage(MascotExpression.Cheer, false)
}

object MascotMapping {
    /** Ordered candidates per expression. Only Confirmed art may appear here (checked by tests). */
    val defaults: Map<MascotExpression, List<String>> = mapOf(
        MascotExpression.Neutral to listOf("listening_finger_on_lips"),
        MascotExpression.Listening to listOf("listening_finger_on_lips", "reading_phone_heart"),
        MascotExpression.Idle to listOf(
            "listening_finger_on_lips", "relaxed_bubble_tea_eyes_closed", "relaxed_lying_chin_hands",
            "playful_lying_wink", "relaxed_bubble_tea_wink"
        ),
        MascotExpression.Talking to listOf("talking_pointing", "talking_speech_bubble"),
        MascotExpression.Greeting to listOf("greeting_peace_wink_burst", "greeting_peace_wink"),
        MascotExpression.Happy to listOf("happy_hands_on_cheeks", "happy_holding_heart"),
        MascotExpression.Excited to listOf("excited_arms_up_stars", "cheer_arms_up", "cheer_glow_stick"),
        MascotExpression.Dancing to listOf("dancing_music_notes"),
        MascotExpression.Sad to listOf("sad_crying"),
        MascotExpression.Angry to listOf("angry_pout_steam", "annoyed_arms_crossed", "grumpy_anger_mark"),
        MascotExpression.Confused to listOf("confused_question_mark", "dizzy_swirl_eyes"),
        MascotExpression.Thinking to listOf("thinking_question_marks"),
        MascotExpression.Surprised to listOf("surprised_exclamation"),
        MascotExpression.Sleepy to listOf("sleepy_hugging_cat"),
        MascotExpression.Sleeping to listOf("sleeping_on_pillow", "sleeping_with_cat"),
        MascotExpression.Gaming to listOf("gaming_controller_focus", "gaming_controller_calm", "gaming_laptop_neutral"),
        MascotExpression.Loading to listOf("working_laptop_focused", "gaming_laptop_neutral"),
        MascotExpression.Thanks to listOf("thanks_sign_th", "happy_hands_on_cheeks"),
        MascotExpression.Cheer to listOf("cheer_glow_stick", "excited_arms_up_stars")
    )
}

/** Picks the art for an event. Developer overrides win; otherwise the default list; otherwise neutral. */
class PoseResolver(private val overrides: Map<String, String> = emptyMap()) {

    fun candidates(expression: MascotExpression, language: AppLanguage?): List<MascotAsset> =
        MascotMapping.defaults[expression].orEmpty()
            .mapNotNull { MascotCatalog.byId(it) }
            .filter { it.review == ReviewState.Confirmed && it.usableIn(language) }

    fun assetFor(expression: MascotExpression, language: AppLanguage?, variant: Int = 0): MascotAsset {
        val list = candidates(expression, language)
        return if (list.isEmpty()) MascotCatalog.neutral else list[Math.floorMod(variant, list.size)]
    }

    fun assetFor(event: MascotEvent, language: AppLanguage?, variant: Int = 0): MascotAsset {
        overrides[event.name]?.let { MascotCatalog.byId(it) }?.takeIf { it.usableIn(language) }?.let { return it }
        return assetFor(event.expression, language, variant)
    }

    fun isOverridden(event: MascotEvent) = event.name in overrides
}

/** Developer pose overrides are stored as a small JSON object: event name to asset id. */
object PoseOverrides {
    fun parse(json: String?): Map<String, String> {
        if (json.isNullOrBlank()) return emptyMap()
        return runCatching {
            val o = JSONObject(json)
            o.keys().asSequence().associateWith { o.getString(it) }
        }.getOrDefault(emptyMap())
            .filter { (event, id) -> MascotEvent.entries.any { it.name == event } && MascotCatalog.byId(id) != null }
    }

    fun toJson(map: Map<String, String>): String = JSONObject(map).toString()
}
