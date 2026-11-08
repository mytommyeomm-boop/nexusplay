package com.nexusplay.launcher.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nexusplay.launcher.core.DeviceInfo
import com.nexusplay.launcher.core.DeviceInfoReader
import com.nexusplay.launcher.ui.components.GlassCard
import com.nexusplay.launcher.ui.components.HwPanel
import com.nexusplay.launcher.ui.components.LampTag
import com.nexusplay.launcher.ui.components.NeonIcons
import com.nexusplay.launcher.ui.components.ScreenHeader
import com.nexusplay.launcher.ui.components.ScreenList
import com.nexusplay.launcher.ui.components.SectionTitle
import com.nexusplay.launcher.ui.components.StatusTag
import com.nexusplay.launcher.ui.components.staggeredEntrance
import com.nexusplay.launcher.ui.theme.NeonCyan
import com.nexusplay.launcher.ui.theme.NeonPurple
import com.nexusplay.launcher.ui.theme.Success
import com.nexusplay.launcher.ui.theme.TextMuted
import com.nexusplay.launcher.ui.theme.TextSecondary
import com.nexusplay.launcher.ui.theme.Warning

@Suppress("DEPRECATION")
private fun appVersion(context: Context): String =
    runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
        .getOrNull() ?: "unknown"

private data class SettingsEntry(val icon: ImageVector, val title: String, val subtitle: String)

private val plannedEntries = listOf(
    SettingsEntry(NeonIcons.Cpu, "Graphics and performance", "Resolution scale, frame limits, power use"),
    SettingsEntry(NeonIcons.Disk, "Storage", "Where games live and how much space they use"),
    SettingsEntry(NeonIcons.Layers, "Interface", "Theme colors and layout density"),
    SettingsEntry(NeonIcons.Bolt, "Runtime and compatibility", "Installed runtimes and per-game profiles"),
    SettingsEntry(NeonIcons.Backup, "Backup and restore", "Export and import your library and settings"),
    SettingsEntry(NeonIcons.Key, "Owner mode", "App name, logo and administration")
)

@Composable
fun SettingsScreen(contentPadding: PaddingValues, onOpenJoystick: () -> Unit) {
    val context = LocalContext.current
    val device = remember { DeviceInfoReader.read(context) }
    val version = remember { appVersion(context) }

    ScreenList(contentPadding) {
        item { ScreenHeader("Settings", "Your device and app preferences") }
        item { Box(Modifier.staggeredEntrance(1)) { DeviceCard(device) } }
        item { SectionTitle("Controls") }
        item {
            Box(Modifier.padding(horizontal = 20.dp).staggeredEntrance(2)) {
                HwPanel(Modifier.fillMaxWidth(), onClick = onOpenJoystick) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Joystick settings", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Size, sensitivity, deadzones, presets and a live test area",
                                style = MaterialTheme.typography.bodySmall, color = TextSecondary
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        LampTag("Live", com.nexusplay.launcher.ui.theme.AccentLime)
                    }
                }
            }
        }
        item { SectionTitle("Preferences") }
        item {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .staggeredEntrance(2),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                plannedEntries.forEach { SettingsRow(it) }
            }
        }
        item {
            Text(
                text = "NexusPlay $version",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DeviceCard(info: DeviceInfo) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("This device", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            if (info.is64BitArm) StatusTag("ARM64", Success) else StatusTag("Not ARM64", Warning)
        }
        Spacer(Modifier.size(6.dp))
        InfoLine("Device", info.model)
        InfoLine("System", info.androidVersion)
        InfoLine("CPU", info.primaryAbi)
        InfoLine("Memory", DeviceInfoReader.formatGb(info.totalRamBytes))
        InfoLine(
            "Free storage",
            "${DeviceInfoReader.formatGb(info.freeStorageBytes)} of ${DeviceInfoReader.formatGb(info.totalStorageBytes)}"
        )
        InfoLine("Vulkan", if (info.hasVulkan) "Reported by device" else "Not reported")
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(Modifier.width(16.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SettingsRow(entry: SettingsEntry) {
    GlassCard(modifier = Modifier.fillMaxWidth(), accent = NeonPurple) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(NeonCyan.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(entry.icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.title, style = MaterialTheme.typography.titleSmall)
                Text(entry.subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Spacer(Modifier.width(10.dp))
            StatusTag("Soon", NeonPurple)
        }
    }
}
