package com.nexusplay.launcher.core

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * A user-facing message stored as a string-resource id plus arguments. Logic code (which has
 * no Context and must stay unit-testable) returns UiText; the screen turns it into Thai only
 * when it is shown. Arguments may themselves be UiText.
 */
class UiText(@StringRes val id: Int, vararg args: Any) {
    val args: List<Any> = args.toList()

    fun resolve(context: Context): String =
        if (args.isEmpty()) context.getString(id)
        else context.getString(id, *args.map { if (it is UiText) it.resolve(context) else it }.toTypedArray())

    override fun equals(other: Any?) = other is UiText && id == other.id && args == other.args
    override fun hashCode() = 31 * id + args.hashCode()
}

@Composable
fun UiText.text(): String = resolve(LocalContext.current)
