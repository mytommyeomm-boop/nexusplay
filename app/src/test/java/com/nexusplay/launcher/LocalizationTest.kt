package com.nexusplay.launcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Guards the two-language rule. Runs on the JVM and reads project files directly:
 * values/ is English (and the fallback for every locale), values-th/ is Thai.
 */
class LocalizationTest {
    private val english = File("src/main/res/values/strings.xml").readText()
    private val thaiText = File("src/main/res/values-th/strings.xml").readText()
    private val thaiChars = Regex("[\\u0E00-\\u0E7F]")
    private val entry = Regex("<string name=\"(\\w+)\"([^>]*)>(.*?)</string>", RegexOption.DOT_MATCHES_ALL)

    // Pure format strings that only wrap technical values, so they carry no Thai text.
    private val formatOnly = setOf("rt_ev_version_fmt", "rt_ev_cpu_arm64", "tut_step")

    private fun translatable(xml: String) = entry.findAll(xml)
        .filter { !it.groupValues[2].contains("translatable=\"false\"") }
        .associate { it.groupValues[1] to it.groupValues[3] }

    @Test fun bothLanguagesDefineTheSameKeys() {
        val en = translatable(english).keys
        val th = translatable(thaiText).keys
        assertEquals("Missing in Thai: ${en - th}", emptySet<String>(), en - th)
        assertEquals("Missing in English: ${th - en}", emptySet<String>(), th - en)
    }

    @Test fun thaiStringsContainThaiAndEnglishStringsDoNot() {
        val badThai = translatable(thaiText).filter { it.key !in formatOnly && !thaiChars.containsMatchIn(it.value) }.keys
        val badEnglish = translatable(english).filter { thaiChars.containsMatchIn(it.value) }.keys
        assertTrue("Thai file without Thai text: $badThai", badThai.isEmpty())
        assertTrue("English file containing Thai: $badEnglish", badEnglish.isEmpty())
    }

    @Test fun placeholdersMatchBetweenLanguages() {
        val ph = Regex("%\\d\\$[sd]")
        val en = translatable(english)
        val bad = translatable(thaiText).filter { (k, v) ->
            en[k]?.let { e -> ph.findAll(e).map { it.value }.sorted().toList() != ph.findAll(v).map { it.value }.sorted().toList() } ?: false
        }.keys
        assertTrue("Placeholder mismatch: $bad", bad.isEmpty())
    }

    @Test fun everyReferencedStringKeyExists() {
        val defined = entry.findAll(english).map { it.groupValues[1] }.toSet()
        val used = File("src/main/java").walkTopDown().filter { it.extension == "kt" }
            .flatMap { Regex("R\\.string\\.(\\w+)").findAll(it.readText()).map { m -> m.groupValues[1] } }.toSet()
        assertTrue("Missing keys: ${used - defined}", (used - defined).isEmpty())
    }

    @Test fun noEnglishLiteralsInTextCalls() {
        val callPattern = Regex("(Text|KeyButton|DeckButton|SectionTitle|ScreenHeader|Field|SliderRow|ToggleRow|Readout)\\(\\s*(text = )?\"[^\"$]*[A-Za-z]{4,}")
        val allowed = setOf("NexusPlay", "Vulkan", "ARM64", "Winlator")
        val hits = File("src/main/java/com/nexusplay/launcher/ui").walkTopDown().filter { it.extension == "kt" }
            .flatMap { f -> f.readLines().mapIndexedNotNull { i, line ->
                if (callPattern.containsMatchIn(line) && allowed.none { line.contains("\"$it\"") }) "${f.name}:${i + 1}" else null
            } }.toList()
        assertTrue("English literals passed to UI calls: $hits", hits.isEmpty())
    }
}
