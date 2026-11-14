# Mascot asset library

39 poses cut from the two supplied sprite sheets (sheet 1 = 5x3 grid, sheet 2 = 6x4 grid). Every crop was segmented from the real alpha channel, stray fragments from neighbouring stickers were removed, and each result was previewed on a checkerboard before assignment. No art was drawn or edited.

Files: `app/src/main/res/drawable-nodpi/mascot_<id>.webp` (lossless, transparent). Runtime catalog: `mascot/MascotCatalog.kt` (generated from the same data). Expression and event mapping: `mascot/MascotMapping.kt`.

| id | source | categories | mouth | review | language | note |
|---|---|---|---|---|---|---|
| `greeting_peace_wink` | s1_01 | Welcome, Happy | Open | Confirmed | any | Wink, open smile, peace sign, pink heart. |
| `gaming_controller_focus` | s1_02 | Gaming | Open | Confirmed | any | Holding a controller, small open mouth. |
| `cheer_arms_up` | s1_03 | Excited, Happy | Open | Confirmed | any | Eyes closed, wide laugh, one arm up. |
| `shy_hands_on_cheeks` | s1_04 | Shy | Closed | Confirmed | any | Blushing, hands on cheeks, mouth closed. |
| `angry_pout_steam` | s1_05 | Angry | Closed | Confirmed | any | Anger mark and steam, pouting, arms crossed. Playful, not hostile. |
| `confused_question_mark` | s1_06 | Confused | Open | Confirmed | any | Big green question mark, finger on chin. |
| `sad_crying` | s1_07 | Sad | Open | Confirmed | any | Crying, tears, hands up. |
| `sleeping_with_cat` | s1_08 | Sleeping | Closed | Confirmed | any | Asleep hugging a black cat, ZZZ. |
| `relaxed_bubble_tea_wink` | s1_09 | Relaxed, Idle | Closed | Confirmed | any | Sipping bubble tea, wink. |
| `happy_holding_heart` | s1_10 | Happy | Open | Confirmed | any | Eyes closed, big smile, holding a pink heart. |
| `gaming_laptop_neutral` | s1_11 | Gaming, Loading | Closed | Confirmed | any | Calm, mouth closed, laptop and controller bubble. |
| `thanks_sign_th` | s1_12 | Notifications | Open | Confirmed | th | Sign reads a Thai thank-you. Thai only. |
| `talking_pointing` | s1_13 | Talking, Tutorial | Open | Confirmed | any | Smiling, pointing at the viewer, mouth open. |
| `surprised_exclamation` | s1_14 | Surprised | Open | Confirmed | any | Wide eyes, open mouth, exclamation and question mark. |
| `shy_hiding_face` | s1_15 | Shy | Hidden | Confirmed | any | Face half hidden behind a sleeve, blushing. |
| `greeting_peace_wink_burst` | s2_01 | Welcome, Happy | Open | Confirmed | any | Wink, open smile, peace sign, green burst. |
| `talking_speech_bubble` | s2_02 | Talking | Open | Confirmed | any | Open smile with a baked-in ellipsis speech bubble. |
| `listening_finger_on_lips` | s2_03 | Listening, Idle | Closed | Confirmed | any | Calm, mouth closed, finger on lips. Reads as quiet and attentive. |
| `scolding_amber_eyes` | s2_04 | Angry | Open | NeedsReview | any | Eyes are amber, not green. Off-model, so not used by default. |
| `annoyed_arms_crossed` | s2_05 | Angry | Closed | Confirmed | any | Arms crossed, steam cloud, mouth closed. |
| `shy_covering_face` | s2_06 | Shy | Hidden | Confirmed | any | Hands covering her face, blushing. |
| `sleeping_on_pillow` | s2_07 | Sleeping | Closed | Confirmed | any | Asleep on a pillow, ZZ. |
| `relaxed_lying_chin_hands` | s2_08 | Relaxed, Idle | Open | Confirmed | any | Lying on her belly, chin on hands, small open mouth. |
| `playful_lying_wink` | s2_09 | Relaxed, Idle | Open | Confirmed | any | Lying with legs up, wink, smile. |
| `excited_arms_up_stars` | s2_10 | Excited, Happy | Open | Confirmed | any | Both arms up, laughing, stars. |
| `dancing_music_notes` | s2_11 | Dancing | Hidden | Confirmed | any | Small full-body figure dancing, music notes. |
| `gaming_controller_calm` | s2_12 | Gaming | Open | Confirmed | any | Holding a controller, calm, small open mouth. |
| `relaxed_bubble_tea_eyes_closed` | s2_13 | Relaxed, Idle | Closed | Confirmed | any | Eyes closed, content, sipping bubble tea. |
| `working_laptop_focused` | s2_14 | Loading, Gaming | Hidden | Confirmed | any | Focused look over a laptop, mouth hidden. |
| `thinking_question_marks` | s2_15 | Thinking, Confused | Open | Confirmed | any | Finger on chin, two question marks, small open mouth. |
| `happy_hands_on_cheeks` | s2_16 | Happy | Open | Confirmed | any | Eyes closed, open smile, hands on cheeks, hearts. |
| `grumpy_anger_mark` | s2_17 | Angry | Open | Confirmed | any | Eyes closed, anger mark, small mouth. Mild. |
| `cheer_glow_stick` | s2_18 | Excited, Cheer | Open | Confirmed | any | Eyes closed, wide laugh, raising a glow stick. |
| `dizzy_swirl_eyes` | s2_19 | Confused | Open | Confirmed | any | Swirl eyes, hands on cheeks, sweat drops. |
| `reading_phone_heart` | s2_20 | Listening | Hidden | Confirmed | any | Looking down at a phone, heart. |
| `sleepy_hugging_cat` | s2_21 | Sleepy, Sleeping | Closed | Confirmed | any | Eyes closed, hugging a black cat, no ZZ. Dozing rather than deep sleep. |
| `announcing_megaphone` | s2_22 | Notifications | Open | NeedsReview | any | Shouting into a megaphone. Eye looks orange, not green. Off-model. |
| `cheer_sign_th` | s2_23 | Cheer | Hidden | NeedsReview | th | Sign has Thai text that is not clearly legible. Verify before use. |
| `peeking_lying_down` | s2_24 | Idle, Listening | Open | Confirmed | any | Lying face-down looking up at the viewer, mouth slightly open. |

## Needs manual review (never chosen automatically)

- `scolding_amber_eyes`: Eyes are amber, not green. Off-model, so not used by default.
- `announcing_megaphone`: Shouting into a megaphone. Eye looks orange, not green. Off-model.
- `cheer_sign_th`: Sign has Thai text that is not clearly legible. Verify before use.

## Not available in the supplied art

- No eyes-closed twin of any pose exists, so real blinking cannot be animated.
- No frame pairs differ only by mouth, so talking is shown by switching between genuine mouth-open and mouth-closed poses, never a fake lip-sync.
- No true waving pose: the closest greeting is the wink with a peace sign.
- No drowsy half-closed-eye pose: `sleepy_hugging_cat` (eyes closed, no ZZ) is the closest.

## Event to pose mapping (default, before developer overrides)

| event | expression | art (Thai UI) | art (English UI) | wired in the app today |
|---|---|---|---|---|
| Welcome | Greeting | `greeting_peace_wink_burst` | `greeting_peace_wink_burst` | yes |
| TutorialExplain | Talking | `talking_pointing` | `talking_pointing` | yes |
| TutorialAdvice | Thinking | `thinking_question_marks` | `thinking_question_marks` | yes |
| TutorialGaming | Gaming | `gaming_controller_focus` | `gaming_controller_focus` | yes |
| TutorialDone | Happy | `happy_hands_on_cheeks` | `happy_hands_on_cheeks` | yes |
| TourSuggestion | Talking | `talking_pointing` | `talking_pointing` | yes |
| NoticeSuccess | Happy | `happy_hands_on_cheeks` | `happy_hands_on_cheeks` | yes |
| NoticeInfo | Talking | `talking_pointing` | `talking_pointing` | yes |
| NoticeWarning | Surprised | `surprised_exclamation` | `surprised_exclamation` | yes |
| NoticeError | Sad | `sad_crying` | `sad_crying` | yes |
| NoticeLoading | Loading | `working_laptop_focused` | `working_laptop_focused` | no (event defined, trigger not built yet) |
| MissingRuntime | Confused | `confused_question_mark` | `confused_question_mark` | yes |
| LaunchFailed | Sad | `sad_crying` | `sad_crying` | yes |
| LaunchHandedOver | Excited | `excited_arms_up_stars` | `excited_arms_up_stars` | no (event defined, trigger not built yet) |
| GamepadSaved | Happy | `happy_hands_on_cheeks` | `happy_hands_on_cheeks` | yes |
| GamepadConfigured | Dancing | `dancing_music_notes` | `dancing_music_notes` | no (event defined, trigger not built yet) |
| GameImported | Happy | `happy_hands_on_cheeks` | `happy_hands_on_cheeks` | no (event defined, trigger not built yet) |
| FirstGameAdded | Dancing | `dancing_music_notes` | `dancing_music_notes` | no (event defined, trigger not built yet) |
| Achievement | Dancing | `dancing_music_notes` | `dancing_music_notes` | no (event defined, trigger not built yet) |
| DailyTip | Thinking | `thinking_question_marks` | `thinking_question_marks` | no (event defined, trigger not built yet) |
| Suggestion | Thinking | `thinking_question_marks` | `thinking_question_marks` | no (event defined, trigger not built yet) |
| QuickHelp | Talking | `talking_pointing` | `talking_pointing` | no (event defined, trigger not built yet) |
| SettingsOpened | Talking | `talking_pointing` | `talking_pointing` | no (event defined, trigger not built yet) |
| GamingMode | Gaming | `gaming_controller_focus` | `gaming_controller_focus` | no (event defined, trigger not built yet) |
| Idle | Idle | `listening_finger_on_lips` | `listening_finger_on_lips` | no (event defined, trigger not built yet) |
| Listening | Listening | `listening_finger_on_lips` | `listening_finger_on_lips` | no (event defined, trigger not built yet) |
| Sleep | Sleeping | `sleeping_on_pillow` | `sleeping_on_pillow` | no (event defined, trigger not built yet) |
| Poked | Surprised | `surprised_exclamation` | `surprised_exclamation` | no (event defined, trigger not built yet) |
| PokedRepeatedly | Angry | `angry_pout_steam` | `angry_pout_steam` | no (event defined, trigger not built yet) |
| Thanks | Thanks | `thanks_sign_th` | `happy_hands_on_cheeks` | no (event defined, trigger not built yet) |
| Encourage | Cheer | `cheer_glow_stick` | `cheer_glow_stick` | no (event defined, trigger not built yet) |

## Expression candidates (first item is the default)

| expression | requires category | mouth rule | candidates |
|---|---|---|---|
| Neutral | Listening | NotOpen | `listening_finger_on_lips` |
| Listening | Listening | NotOpen | `listening_finger_on_lips`, `reading_phone_heart` |
| Idle | Idle | Any | `listening_finger_on_lips`, `relaxed_bubble_tea_eyes_closed`, `relaxed_lying_chin_hands`, `playful_lying_wink`, `relaxed_bubble_tea_wink` |
| Talking | Talking | Open | `talking_pointing`, `talking_speech_bubble` |
| Greeting | Welcome | Any | `greeting_peace_wink_burst`, `greeting_peace_wink` |
| Happy | Happy | Any | `happy_hands_on_cheeks`, `happy_holding_heart` |
| Excited | Excited | Any | `excited_arms_up_stars`, `cheer_arms_up`, `cheer_glow_stick` |
| Dancing | Dancing | Any | `dancing_music_notes` |
| Sad | Sad | Any | `sad_crying` |
| Angry | Angry | Any | `angry_pout_steam`, `annoyed_arms_crossed`, `grumpy_anger_mark` |
| Confused | Confused | Any | `confused_question_mark`, `dizzy_swirl_eyes` |
| Thinking | Thinking | Any | `thinking_question_marks` |
| Surprised | Surprised | Any | `surprised_exclamation` |
| Sleepy | Sleepy | NotOpen | `sleepy_hugging_cat` |
| Sleeping | Sleeping | NotOpen | `sleeping_on_pillow`, `sleeping_with_cat` |
| Gaming | Gaming | Any | `gaming_controller_focus`, `gaming_controller_calm`, `gaming_laptop_neutral` |
| Loading | Loading | Any | `working_laptop_focused`, `gaming_laptop_neutral` |
| Thanks | Happy, Notifications | Any | `thanks_sign_th`, `happy_hands_on_cheeks` |
| Cheer | Cheer, Excited | Any | `cheer_glow_stick`, `excited_arms_up_stars` |
