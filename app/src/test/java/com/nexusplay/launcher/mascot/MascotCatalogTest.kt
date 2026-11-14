package com.nexusplay.launcher.mascot

import com.nexusplay.launcher.core.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Guards the pose rules: art may only be used for an expression it genuinely matches. */
class MascotCatalogTest {
    private val languages = listOf(AppLanguage.Thai, AppLanguage.English, null)

    @Test fun allThirtyNineAssetsAreRegisteredWithUniqueIds() {
        assertEquals(39, MascotCatalog.all.size)
        assertEquals(39, MascotCatalog.all.map { it.id }.toSet().size)
        assertEquals(39, MascotCatalog.all.map { it.res }.toSet().size)
    }

    @Test fun defaultListsOnlyContainConfirmedArtThatMatchesItsExpression() {
        MascotMapping.defaults.forEach { (expression, ids) ->
            ids.forEach { id ->
                val asset = MascotCatalog.byId(id)
                assertNotNull("Unknown asset $id", asset)
                assertEquals("$id must be Confirmed", ReviewState.Confirmed, asset!!.review)
                assertTrue("$id does not match $expression", expression.accepts(asset))
            }
        }
    }

    @Test fun everyExpressionHasUsableArtInEveryLanguage() {
        for (language in languages) for (e in MascotExpression.entries) {
            assertTrue("$e has no art for $language", PoseResolver().candidates(e, language).isNotEmpty())
        }
    }

    @Test fun everyEventResolvesToArtThatMatchesItsExpression() {
        for (language in languages) for (event in MascotEvent.entries) {
            val asset = PoseResolver().assetFor(event, language)
            assertTrue("$event -> ${asset.id} ($language)", event.expression.accepts(asset))
        }
    }

    @Test fun talkingIsAlwaysMouthOpenAndListeningSleepingAreNeverMouthOpen() {
        PoseResolver().candidates(MascotExpression.Talking, null).forEach { assertEquals(Mouth.Open, it.mouth) }
        listOf(MascotExpression.Listening, MascotExpression.Neutral, MascotExpression.Sleeping, MascotExpression.Sleepy).forEach { e ->
            PoseResolver().candidates(e, null).forEach { assertTrue("${it.id} for $e", it.mouth != Mouth.Open) }
        }
    }

    @Test fun angryIsOnlyForPlayfulPoutingNeverForErrors() {
        val angry = MascotEvent.entries.filter { it.expression == MascotExpression.Angry }
        assertEquals(listOf(MascotEvent.PokedRepeatedly), angry)
        assertEquals(MascotExpression.Sad, MascotEvent.NoticeError.expression)
        assertEquals(MascotExpression.Sad, MascotEvent.LaunchFailed.expression)
    }

    @Test fun emotionsMatchTheirSituations() {
        assertEquals(MascotExpression.Happy, MascotEvent.NoticeSuccess.expression)
        assertEquals(MascotExpression.Happy, MascotEvent.GamepadSaved.expression)
        assertEquals(MascotExpression.Dancing, MascotEvent.Achievement.expression)
        assertEquals(MascotExpression.Greeting, MascotEvent.Welcome.expression)
        assertEquals(MascotExpression.Confused, MascotEvent.MissingRuntime.expression)
        assertEquals(MascotExpression.Surprised, MascotEvent.NoticeWarning.expression)
        assertEquals(MascotExpression.Gaming, MascotEvent.GamingMode.expression)
        assertEquals(MascotExpression.Sleeping, MascotEvent.Sleep.expression)
        assertEquals(MascotExpression.Loading, MascotEvent.NoticeLoading.expression)
    }

    @Test fun thaiOnlyArtIsNeverUsedInEnglishAndReviewArtIsNeverAutomatic() {
        MascotEvent.entries.forEach {
            assertFalse(PoseResolver().assetFor(it, AppLanguage.English).language == AppLanguage.Thai)
            assertEquals(ReviewState.Confirmed, PoseResolver().assetFor(it, AppLanguage.Thai).review)
        }
        assertEquals("thanks_sign_th", PoseResolver().assetFor(MascotEvent.Thanks, AppLanguage.Thai).id)
        assertEquals("happy_hands_on_cheeks", PoseResolver().assetFor(MascotEvent.Thanks, AppLanguage.English).id)
    }

    @Test fun neutralIsCalmAndMouthClosed() {
        assertEquals(Mouth.Closed, MascotCatalog.neutral.mouth)
        assertTrue(MascotCategory.Listening in MascotCatalog.neutral.categories)
    }

    @Test fun developerOverridesWinButRespectLanguage() {
        val r = PoseResolver(mapOf("Welcome" to "dancing_music_notes", "NoticeSuccess" to "thanks_sign_th"))
        assertEquals("dancing_music_notes", r.assetFor(MascotEvent.Welcome, AppLanguage.English).id)
        // Thai-only art cannot be forced into English: falls back to the normal mapping.
        assertEquals("happy_hands_on_cheeks", r.assetFor(MascotEvent.NoticeSuccess, AppLanguage.English).id)
        assertTrue(r.isOverridden(MascotEvent.Welcome))
    }

    @Test fun overridesSurviveJsonAndBadEntriesAreDropped() {
        val saved = PoseOverrides.toJson(mapOf("Welcome" to "dancing_music_notes"))
        assertEquals(mapOf("Welcome" to "dancing_music_notes"), PoseOverrides.parse(saved))
        assertTrue(PoseOverrides.parse("""{"NoSuchEvent":"dancing_music_notes","Welcome":"missing_art"}""").isEmpty())
        assertTrue(PoseOverrides.parse("not json").isEmpty())
        assertTrue(PoseOverrides.parse(null).isEmpty())
    }
}
