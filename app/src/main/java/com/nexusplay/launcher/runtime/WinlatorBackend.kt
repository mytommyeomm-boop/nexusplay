package com.nexusplay.launcher.runtime

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.pm.PackageInfoCompat
import com.nexusplay.launcher.core.AppLog

/**
 * Winlator family. What is relied on, and where it comes from (public sources, not yet run on a
 * device by this app): the game screen is `<entryClass>`; it is exported only in the Cmod lineage;
 * when exported it reads a `shortcut_path` string extra naming a Winlator shortcut (.desktop) file.
 * Export state is always read from the installed app, never assumed from the package name.
 */
class WinlatorBackend(
    override val id: String,
    override val displayName: String,
    override val packageName: String,
    private val entryClass: String,
    override val projectUrl: String,
    override val setupSteps: List<String>,
    override val knownLimits: List<String>
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
            ?: return fail("$displayName has no launcher entry on this device.")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            AppLog.i("Runtime", "Opened $displayName")
            LaunchResult.HandedOver("Opened $displayName. Start your game from inside it.")
        } catch (e: ActivityNotFoundException) {
            fail("Android could not open $displayName.")
        } catch (e: SecurityException) {
            fail("Android refused to open $displayName: ${e.message}")
        }
    }

    override fun launchShortcut(context: Context, shortcutPath: String): LaunchResult {
        ShortcutPath.validate(shortcutPath)?.let { return fail(it) }
        val d = detect(context)
        val caps = RuntimeAssessor.capabilities(d)
        if (!d.installed) return fail("$displayName is not installed.")
        if (!d.entryFound) return fail("This $displayName build has no $entryClass, so it cannot be launched this way.")
        if (!caps.canLaunchShortcut) {
            return fail("This $displayName build keeps its game screen private, so other apps cannot launch it. Open the app and start the game there.")
        }
        val intent = Intent().apply {
            component = ComponentName(packageName, entryClass)
            putExtra("shortcut_path", shortcutPath.trim())
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val fileName = shortcutPath.trim().substringAfterLast('/')
        return try {
            context.startActivity(intent)
            AppLog.i("Runtime", "Handed $fileName to $displayName")
            LaunchResult.HandedOver(
                "Android delivered the request to $displayName. The runtime reports nothing back, " +
                    "so whether the game started, and when it exits, is unknown."
            )
        } catch (e: ActivityNotFoundException) {
            fail("Android could not find $displayName's game screen.")
        } catch (e: SecurityException) {
            fail("Android refused the launch: ${e.message}")
        }
    }

    private fun fail(reason: String): LaunchResult.Failed {
        AppLog.w("Runtime", reason)
        return LaunchResult.Failed(reason)
    }
}
