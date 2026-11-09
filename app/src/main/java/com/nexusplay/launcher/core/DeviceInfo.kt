package com.nexusplay.launcher.core

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale

/** Real hardware facts used by the compatibility checks added in later stages. */
data class DeviceInfo(
    val model: String,
    val androidVersion: String,
    val primaryAbi: String,
    val is64BitArm: Boolean,
    val totalRamBytes: Long,
    val freeStorageBytes: Long,
    val totalStorageBytes: Long,
    val hasVulkan: Boolean
) {
    /** 0f..1f share of internal storage already in use. */
    val storageUsedFraction: Float
        get() = if (totalStorageBytes > 0) 1f - freeStorageBytes.toFloat() / totalStorageBytes else 0f
}

/** Read once in NexusApp and shared with every screen. */
val LocalDeviceInfo = staticCompositionLocalOf<DeviceInfo> {
    error("DeviceInfo was not provided")
}

object DeviceInfoReader {

    fun read(context: Context): DeviceInfo {
        val memory = ActivityManager.MemoryInfo()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.getMemoryInfo(memory)

        val stat = StatFs(Environment.getDataDirectory().path)
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.titlecase(Locale.ROOT) }

        return DeviceInfo(
            model = if (Build.MODEL.startsWith(Build.MANUFACTURER, ignoreCase = true)) Build.MODEL
            else "$manufacturer ${Build.MODEL}",
            androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            primaryAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown",
            is64BitArm = Build.SUPPORTED_64_BIT_ABIS.contains("arm64-v8a"),
            totalRamBytes = memory.totalMem,
            freeStorageBytes = stat.availableBytes,
            totalStorageBytes = stat.totalBytes,
            hasVulkan = context.packageManager.hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_VERSION)
        )
    }

    fun formatGb(bytes: Long): String =
        String.format(Locale.US, "%.1f GB", bytes / 1_073_741_824.0)
}
