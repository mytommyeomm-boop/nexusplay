package com.nexusplay.launcher.core

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import java.util.Locale

enum class AppLanguage(val tag: String) {
    Thai("th"),
    English("en");

    companion object {
        fun fromTag(tag: String?): AppLanguage? = entries.firstOrNull { it.tag == tag }
    }
}

/**
 * Single place that decides the app language. The choice lives in SharedPreferences (internal
 * storage), so it survives restarts, phone reboots and app updates. It is applied by wrapping the
 * Activity base context ([wrap]) and the Activity is recreated whenever the language changes.
 */
object LanguageManager {
    private const val PREFS = "app_prefs"
    private const val KEY = "language"

    /** The saved language, or null on first launch (no choice made yet). */
    fun saved(context: Context): AppLanguage? =
        AppLanguage.fromTag(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null))

    /** commit(), not apply(): the Activity restarts right away and must read the new value. */
    fun save(context: Context, language: AppLanguage): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, language.tag).commit()

    /** Saves, then restarts the Activity so every screen is rebuilt in the new language. */
    fun apply(activity: Activity, language: AppLanguage): Boolean {
        val ok = save(activity, language)
        if (ok) activity.recreate()
        return ok
    }

    /** Pre-selection for the first-launch screen: Thai on Thai phones, otherwise English. */
    fun suggested(context: Context): AppLanguage =
        if (context.resources.configuration.locales[0].language == "th") AppLanguage.Thai else AppLanguage.English

    /** A context whose resources use [language]. Used for the live preview on the picker. */
    fun localized(base: Context, language: AppLanguage): Context {
        val config = Configuration(base.resources.configuration)
        config.setLocale(Locale.forLanguageTag(language.tag))
        return base.createConfigurationContext(config)
    }

    /** Called from Activity.attachBaseContext. Does nothing until the user has chosen a language. */
    fun wrap(base: Context): Context {
        val language = saved(base) ?: return base
        Locale.setDefault(Locale.forLanguageTag(language.tag))
        return localized(base, language)
    }
}

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
