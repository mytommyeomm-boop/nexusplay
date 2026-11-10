package com.nexusplay.launcher.runtime

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.pm.PackageInfoCompat
import com.nexusplay.launcher.R
import com.nexusplay.launcher.core.AppLog
import com.nexusplay.launcher.core.UiText

/**
 * Winlator family. What is relied on, and where it comes from (public sources, not yet run on a
 * device by this app): the game screen is `<entryClass>`; it is exported only in the Cmod lineage;
 * when exported it reads a `shortcut_path` string extra naming a Winlator shortcut (.desktop) file.
 * Export state is always read from the installed app, never assumed from the package name.
 *
 * Every failure carries Thai text (what happened, why, what to do) for the user; the English
 * technical detail goes to the log only.
 */
class WinlatorBackend(
    override val id: String,
    override val displayName: String,
    override val packageName: String,
    private val entryClass: String,
    override val projectUrl: String,
    override val setupSteps: List<UiText>,
    override val knownLimits: List<UiText>
) : RuntimeBackend {

    override fun detect(context: Context): RuntimeDetection {
        val pm = context.packageManager
        val info = try {
            pm.getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            return RuntimeDetection.Missing
        }
        val entry = try {
            pm.getActivityInfo(ComponentName(packageName, entryClass), 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
        return RuntimeDetection(
            installed = true,
            versionName = info.versionName,
            versionCode = PackageInfoCompat.getLongVersionCode(info),
            minSdk = info.applicationInfo?.minSdkVersion,
            hasLauncher = pm.getLaunchIntentForPackage(packageName) != null,
            entryFound = entry != null,
            entryExported = entry?.exported == true,
            entryEnabled = entry?.enabled == true
        )
    }

    override fun openApp(context: Context): LaunchResult {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            ?: return fail(UiText(R.string.rt_err_no_launcher, displayName), "no launcher intent for $packageName")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            AppLog.i("Runtime", UiText(R.string.log_opened, displayName), "opened $packageName")
            LaunchResult.HandedOver(UiText(R.string.rt_ok_opened, displayName))
        } catch (e: ActivityNotFoundException) {
            fail(UiText(R.string.rt_err_cannot_open, displayName), "ActivityNotFoundException: ${e.message}")
        } catch (e: SecurityException) {
            fail(UiText(R.string.rt_err_denied_open, displayName), "SecurityException: ${e.message}")
        }
    }

    override fun launchShortcut(context: Context, shortcutPath: String): LaunchResult {
        ShortcutPath.validate(shortcutPath)?.let { return fail(it, "invalid shortcut path") }
        val d = detect(context)
        val caps = RuntimeAssessor.capabilities(d)
        if (!d.installed) return fail(UiText(R.string.rt_err_not_installed, displayName), "$packageName not installed")
        if (!d.entryFound) {
            return fail(UiText(R.string.rt_err_no_entry, displayName, entryClass), "entry class $entryClass not found")
        }
        if (!caps.canLaunchShortcut) {
            return fail(UiText(R.string.rt_err_private, displayName), "entry not exported or disabled")
        }
        val intent = Intent().apply {
            component = ComponentName(packageName, entryClass)
            putExtra("shortcut_path", shortcutPath.trim())
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val fileName = shortcutPath.trim().substringAfterLast('/')
        return try {
            context.startActivity(intent)
            AppLog.i("Runtime", UiText(R.string.log_handed, displayName), "handed $fileName to $packageName")
            LaunchResult.HandedOver(UiText(R.string.rt_ok_handed, displayName))
        } catch (e: ActivityNotFoundException) {
            fail(UiText(R.string.rt_err_activity_missing, displayName), "ActivityNotFoundException: ${e.message}")
        } catch (e: SecurityException) {
            fail(UiText(R.string.rt_err_denied_launch), "SecurityException: ${e.message}")
        }
    }

    private fun fail(message: UiText, raw: String): LaunchResult.Failed {
        AppLog.w("Runtime", message, raw)
        return LaunchResult.Failed(message)
    }
}
