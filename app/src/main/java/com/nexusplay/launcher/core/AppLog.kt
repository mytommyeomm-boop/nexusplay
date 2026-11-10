package com.nexusplay.launcher.core

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.compositionLocalOf

/**
 * Small in-memory event log (last 200 entries) shown in the app and, later, exported as diagnostics.
 * Rule: never log file paths or user content, only file names and outcomes.
 */
object AppLog {
    data class Entry(val timeMillis: Long, val level: String, val tag: String, val message: String)

    private const val MAX = 200
    private val ring = ArrayDeque<Entry>()

    var entries by mutableStateOf<List<Entry>>(emptyList())
        private set

    fun i(tag: String, message: String) = add("INFO", tag, message)
    fun w(tag: String, message: String) = add("WARN", tag, message)
    fun e(tag: String, message: String) = add("ERROR", tag, message)

    @Synchronized
    private fun add(level: String, tag: String, message: String) {
        ring.addLast(Entry(System.currentTimeMillis(), level, tag, message))
        while (ring.size > MAX) ring.removeFirst()
        entries = ring.toList()
        when (level) {
            "ERROR" -> Log.e("NexusPlay/$tag", message)
            "WARN" -> Log.w("NexusPlay/$tag", message)
            else -> Log.i("NexusPlay/$tag", message)
        }
    }
}

/** Incremented every time the app returns to the foreground, so screens can re-check the system. */
val LocalResumeTick = compositionLocalOf { 0 }
