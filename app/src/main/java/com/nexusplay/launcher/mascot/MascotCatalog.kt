package com.nexusplay.launcher.mascot

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.nexusplay.launcher.R
import com.nexusplay.launcher.core.AppLanguage

// GENERATED from tools/mascot manifest (see docs/MASCOT_ASSETS.md). Names live in strings.xml (asset_<id>).
// Edit the manifest and regenerate rather than changing ids here by hand.

enum class MascotCategory(@StringRes val labelRes: Int) {
    Idle(R.string.cat_idle),
    Talking(R.string.cat_talking),
    Listening(R.string.cat_listening),
    Welcome(R.string.cat_welcome),
    Happy(R.string.cat_happy),
    Sad(R.string.cat_sad),
    Angry(R.string.cat_angry),
    Confused(R.string.cat_confused),
    Surprised(R.string.cat_surprised),
    Sleepy(R.string.cat_sleepy),
    Sleeping(R.string.cat_sleeping),
    Dancing(R.string.cat_dancing),
    Gaming(R.string.cat_gaming),
    Thinking(R.string.cat_thinking),
    Notifications(R.string.cat_notifications),
    Loading(R.string.cat_loading),
    Tutorial(R.string.cat_tutorial),
    Relaxed(R.string.cat_relaxed),
    Shy(R.string.cat_shy),
    Excited(R.string.cat_excited),
    Cheer(R.string.cat_cheer);
}

/** What the mouth shows in the art. Hidden means a prop or hand covers it, so it is not a talk or listen pose. */
enum class Mouth(@StringRes val labelRes: Int) {
    Open(R.string.mouth_open), Closed(R.string.mouth_closed), Hidden(R.string.mouth_hidden)
}

/** NeedsReview art is kept for inspection but never chosen automatically. */
enum class ReviewState { Confirmed, NeedsReview }

class MascotAsset(
    val id: String,
    @DrawableRes val res: Int,
    @StringRes val nameRes: Int,
    val categories: Set<MascotCategory>,
    val mouth: Mouth,
    val review: ReviewState,
    val language: AppLanguage?,
    val note: String
) {
    /** Art with baked-in text only fits its own language. */
    fun usableIn(language: AppLanguage?): Boolean = this.language == null || this.language == language
}

object MascotCatalog {
    val all: List<MascotAsset> = listOf(
        MascotAsset("greeting_peace_wink", R.drawable.mascot_greeting_peace_wink, R.string.asset_greeting_peace_wink, setOf(MascotCategory.Welcome, MascotCategory.Happy), Mouth.Open, ReviewState.Confirmed, null, "Wink, open smile, peace sign, pink heart."),
        MascotAsset("gaming_controller_focus", R.drawable.mascot_gaming_controller_focus, R.string.asset_gaming_controller_focus, setOf(MascotCategory.Gaming), Mouth.Open, ReviewState.Confirmed, null, "Holding a controller, small open mouth."),
        MascotAsset("cheer_arms_up", R.drawable.mascot_cheer_arms_up, R.string.asset_cheer_arms_up, setOf(MascotCategory.Excited, MascotCategory.Happy), Mouth.Open, ReviewState.Confirmed, null, "Eyes closed, wide laugh, one arm up."),
        MascotAsset("shy_hands_on_cheeks", R.drawable.mascot_shy_hands_on_cheeks, R.string.asset_shy_hands_on_cheeks, setOf(MascotCategory.Shy), Mouth.Closed, ReviewState.Confirmed, null, "Blushing, hands on cheeks, mouth closed."),
        MascotAsset("angry_pout_steam", R.drawable.mascot_angry_pout_steam, R.string.asset_angry_pout_steam, setOf(MascotCategory.Angry), Mouth.Closed, ReviewState.Confirmed, null, "Anger mark and steam, pouting, arms crossed. Playful, not hostile."),
        MascotAsset("confused_question_mark", R.drawable.mascot_confused_question_mark, R.string.asset_confused_question_mark, setOf(MascotCategory.Confused), Mouth.Open, ReviewState.Confirmed, null, "Big green question mark, finger on chin."),
        MascotAsset("sad_crying", R.drawable.mascot_sad_crying, R.string.asset_sad_crying, setOf(MascotCategory.Sad), Mouth.Open, ReviewState.Confirmed, null, "Crying, tears, hands up."),
        MascotAsset("sleeping_with_cat", R.drawable.mascot_sleeping_with_cat, R.string.asset_sleeping_with_cat, setOf(MascotCategory.Sleeping), Mouth.Closed, ReviewState.Confirmed, null, "Asleep hugging a black cat, ZZZ."),
        MascotAsset("relaxed_bubble_tea_wink", R.drawable.mascot_relaxed_bubble_tea_wink, R.string.asset_relaxed_bubble_tea_wink, setOf(MascotCategory.Relaxed, MascotCategory.Idle), Mouth.Closed, ReviewState.Confirmed, null, "Sipping bubble tea, wink."),
        MascotAsset("happy_holding_heart", R.drawable.mascot_happy_holding_heart, R.string.asset_happy_holding_heart, setOf(MascotCategory.Happy), Mouth.Open, ReviewState.Confirmed, null, "Eyes closed, big smile, holding a pink heart."),
        MascotAsset("gaming_laptop_neutral", R.drawable.mascot_gaming_laptop_neutral, R.string.asset_gaming_laptop_neutral, setOf(MascotCategory.Gaming, MascotCategory.Loading), Mouth.Closed, ReviewState.Confirmed, null, "Calm, mouth closed, laptop and controller bubble."),
        MascotAsset("thanks_sign_th", R.drawable.mascot_thanks_sign_th, R.string.asset_thanks_sign_th, setOf(MascotCategory.Notifications), Mouth.Open, ReviewState.Confirmed, AppLanguage.Thai, "Sign reads a Thai thank-you. Thai only."),
        MascotAsset("talking_pointing", R.drawable.mascot_talking_pointing, R.string.asset_talking_pointing, setOf(MascotCategory.Talking, MascotCategory.Tutorial), Mouth.Open, ReviewState.Confirmed, null, "Smiling, pointing at the viewer, mouth open."),
        MascotAsset("surprised_exclamation", R.drawable.mascot_surprised_exclamation, R.string.asset_surprised_exclamation, setOf(MascotCategory.Surprised), Mouth.Open, ReviewState.Confirmed, null, "Wide eyes, open mouth, exclamation and question mark."),
        MascotAsset("shy_hiding_face", R.drawable.mascot_shy_hiding_face, R.string.asset_shy_hiding_face, setOf(MascotCategory.Shy), Mouth.Hidden, ReviewState.Confirmed, null, "Face half hidden behind a sleeve, blushing."),
        MascotAsset("greeting_peace_wink_burst", R.drawable.mascot_greeting_peace_wink_burst, R.string.asset_greeting_peace_wink_burst, setOf(MascotCategory.Welcome, MascotCategory.Happy), Mouth.Open, ReviewState.Confirmed, null, "Wink, open smile, peace sign, green burst."),
        MascotAsset("talking_speech_bubble", R.drawable.mascot_talking_speech_bubble, R.string.asset_talking_speech_bubble, setOf(MascotCategory.Talking), Mouth.Open, ReviewState.Confirmed, null, "Open smile with a baked-in ellipsis speech bubble."),
        MascotAsset("listening_finger_on_lips", R.drawable.mascot_listening_finger_on_lips, R.string.asset_listening_finger_on_lips, setOf(MascotCategory.Listening, MascotCategory.Idle), Mouth.Closed, ReviewState.Confirmed, null, "Calm, mouth closed, finger on lips. Reads as quiet and attentive."),
        MascotAsset("scolding_amber_eyes", R.drawable.mascot_scolding_amber_eyes, R.string.asset_scolding_amber_eyes, setOf(MascotCategory.Angry), Mouth.Open, ReviewState.NeedsReview, null, "Eyes are amber, not green. Off-model, so not used by default."),
        MascotAsset("annoyed_arms_crossed", R.drawable.mascot_annoyed_arms_crossed, R.string.asset_annoyed_arms_crossed, setOf(MascotCategory.Angry), Mouth.Closed, ReviewState.Confirmed, null, "Arms crossed, steam cloud, mouth closed."),
        MascotAsset("shy_covering_face", R.drawable.mascot_shy_covering_face, R.string.asset_shy_covering_face, setOf(MascotCategory.Shy), Mouth.Hidden, ReviewState.Confirmed, null, "Hands covering her face, blushing."),
        MascotAsset("sleeping_on_pillow", R.drawable.mascot_sleeping_on_pillow, R.string.asset_sleeping_on_pillow, setOf(MascotCategory.Sleeping), Mouth.Closed, ReviewState.Confirmed, null, "Asleep on a pillow, ZZ."),
        MascotAsset("relaxed_lying_chin_hands", R.drawable.mascot_relaxed_lying_chin_hands, R.string.asset_relaxed_lying_chin_hands, setOf(MascotCategory.Relaxed, MascotCategory.Idle), Mouth.Open, ReviewState.Confirmed, null, "Lying on her belly, chin on hands, small open mouth."),
        MascotAsset("playful_lying_wink", R.drawable.mascot_playful_lying_wink, R.string.asset_playful_lying_wink, setOf(MascotCategory.Relaxed, MascotCategory.Idle), Mouth.Open, ReviewState.Confirmed, null, "Lying with legs up, wink, smile."),
        MascotAsset("excited_arms_up_stars", R.drawable.mascot_excited_arms_up_stars, R.string.asset_excited_arms_up_stars, setOf(MascotCategory.Excited, MascotCategory.Happy), Mouth.Open, ReviewState.Confirmed, null, "Both arms up, laughing, stars."),
        MascotAsset("dancing_music_notes", R.drawable.mascot_dancing_music_notes, R.string.asset_dancing_music_notes, setOf(MascotCategory.Dancing), Mouth.Hidden, ReviewState.Confirmed, null, "Small full-body figure dancing, music notes."),
        MascotAsset("gaming_controller_calm", R.drawable.mascot_gaming_controller_calm, R.string.asset_gaming_controller_calm, setOf(MascotCategory.Gaming), Mouth.Open, ReviewState.Confirmed, null, "Holding a controller, calm, small open mouth."),
        MascotAsset("relaxed_bubble_tea_eyes_closed", R.drawable.mascot_relaxed_bubble_tea_eyes_closed, R.string.asset_relaxed_bubble_tea_eyes_closed, setOf(MascotCategory.Relaxed, MascotCategory.Idle), Mouth.Closed, ReviewState.Confirmed, null, "Eyes closed, content, sipping bubble tea."),
        MascotAsset("working_laptop_focused", R.drawable.mascot_working_laptop_focused, R.string.asset_working_laptop_focused, setOf(MascotCategory.Loading, MascotCategory.Gaming), Mouth.Hidden, ReviewState.Confirmed, null, "Focused look over a laptop, mouth hidden."),
        MascotAsset("thinking_question_marks", R.drawable.mascot_thinking_question_marks, R.string.asset_thinking_question_marks, setOf(MascotCategory.Thinking, MascotCategory.Confused), Mouth.Open, ReviewState.Confirmed, null, "Finger on chin, two question marks, small open mouth."),
        MascotAsset("happy_hands_on_cheeks", R.drawable.mascot_happy_hands_on_cheeks, R.string.asset_happy_hands_on_cheeks, setOf(MascotCategory.Happy), Mouth.Open, ReviewState.Confirmed, null, "Eyes closed, open smile, hands on cheeks, hearts."),
        MascotAsset("grumpy_anger_mark", R.drawable.mascot_grumpy_anger_mark, R.string.asset_grumpy_anger_mark, setOf(MascotCategory.Angry), Mouth.Open, ReviewState.Confirmed, null, "Eyes closed, anger mark, small mouth. Mild."),
        MascotAsset("cheer_glow_stick", R.drawable.mascot_cheer_glow_stick, R.string.asset_cheer_glow_stick, setOf(MascotCategory.Excited, MascotCategory.Cheer), Mouth.Open, ReviewState.Confirmed, null, "Eyes closed, wide laugh, raising a glow stick."),
        MascotAsset("dizzy_swirl_eyes", R.drawable.mascot_dizzy_swirl_eyes, R.string.asset_dizzy_swirl_eyes, setOf(MascotCategory.Confused), Mouth.Open, ReviewState.Confirmed, null, "Swirl eyes, hands on cheeks, sweat drops."),
        MascotAsset("reading_phone_heart", R.drawable.mascot_reading_phone_heart, R.string.asset_reading_phone_heart, setOf(MascotCategory.Listening), Mouth.Hidden, ReviewState.Confirmed, null, "Looking down at a phone, heart."),
        MascotAsset("sleepy_hugging_cat", R.drawable.mascot_sleepy_hugging_cat, R.string.asset_sleepy_hugging_cat, setOf(MascotCategory.Sleepy, MascotCategory.Sleeping), Mouth.Closed, ReviewState.Confirmed, null, "Eyes closed, hugging a black cat, no ZZ. Dozing rather than deep sleep."),
        MascotAsset("announcing_megaphone", R.drawable.mascot_announcing_megaphone, R.string.asset_announcing_megaphone, setOf(MascotCategory.Notifications), Mouth.Open, ReviewState.NeedsReview, null, "Shouting into a megaphone. Eye looks orange, not green. Off-model."),
        MascotAsset("cheer_sign_th", R.drawable.mascot_cheer_sign_th, R.string.asset_cheer_sign_th, setOf(MascotCategory.Cheer), Mouth.Hidden, ReviewState.NeedsReview, AppLanguage.Thai, "Sign has Thai text that is not clearly legible. Verify before use."),
        MascotAsset("peeking_lying_down", R.drawable.mascot_peeking_lying_down, R.string.asset_peeking_lying_down, setOf(MascotCategory.Idle, MascotCategory.Listening), Mouth.Open, ReviewState.Confirmed, null, "Lying face-down looking up at the viewer, mouth slightly open.")
    )

    private val index = all.associateBy { it.id }

    fun byId(id: String?): MascotAsset? = index[id]

    /** Used whenever no suitable pose exists: calm, mouth closed, no strong emotion. */
    val neutral: MascotAsset get() = index.getValue("listening_finger_on_lips")
}
