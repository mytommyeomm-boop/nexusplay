package com.nexusplay.launcher.core

import android.util.Log
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Small in-memory event log (last 200 entries). [Entry.message] is Thai text shown in the app;
 * [Entry.raw] keeps the English technical detail for diagnostics and logcat, kept separate on purpose.
 * Rule: never log file paths or user content, only file names and outcomes.
 */
object AppLog {
    data class Entry(
        val timeMillis: Long,
        val level: String,
        val tag: String,
        val message: UiText,
        val raw: String
    )

    private const val MAX = 200
    private val ring = ArrayDeque<Entry>()

    var entries by mutableStateOf<List<Entry>>(emptyList())
        private set

    fun i(tag: String, message: UiText, raw: String = "") = add("INFO", tag, message, raw)
    fun w(tag: String, message: UiText, raw: String = "") = add("WARN", tag, message, raw)
    fun e(tag: String, message: UiText, raw: String = "") = add("ERROR", tag, message, raw)

    @Synchronized
    private fun add(level: String, tag: String, message: UiText, raw: String) {
        ring.addLast(Entry(System.currentTimeMillis(), level, tag, message, raw))
        while (ring.size > MAX) ring.removeFirst()
        entries = ring.toList()
        val line = raw.ifEmpty { "message#${message.id}" }
        when (level) {
            "ERROR" -> Log.e("NexusPlay/$tag", line)
            "WARN" -> Log.w("NexusPlay/$tag", line)
            else -> Log.i("NexusPlay/$tag", line)
        }
    }
}

/** Incremented every time the app returns to the foreground, so screens can re-check the system. */
val LocalResumeTick = compositionLocalOf { 0 }
