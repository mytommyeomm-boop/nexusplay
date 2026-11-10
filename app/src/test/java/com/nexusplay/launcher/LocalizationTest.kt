package com.nexusplay.launcher

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Guards the Thai-first rule. Runs on the JVM and reads the project files directly. */
class LocalizationTest {
    private val strings = File("src/main/res/values/strings.xml").readText()
    private val thai = Regex("[\\u0E00-\\u0E7F]")
    private val entry = Regex("<string name=\"(\\w+)\"([^>]*)>(.*?)</string>", RegexOption.DOT_MATCHES_ALL)

    // Pure format strings that only wrap technical values.
    private val formatOnly = setOf("rt_ev_version_fmt", "rt_ev_cpu_arm64")

    @Test fun everyTranslatableStringContainsThai() {
        val bad = entry.findAll(strings)
            .filter { !it.groupValues[2].contains("translatable=\"false\"") }
            .filter { it.groupValues[1] !in formatOnly }
            .filter { !thai.containsMatchIn(it.groupValues[3]) }
            .map { it.groupValues[1] }.toList()
        assertTrue("Strings without Thai text: $bad", bad.isEmpty())
    }

    @Test fun everyReferencedStringKeyExists() {
        val defined = entry.findAll(strings).map { it.groupValues[1] }.toSet()
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
