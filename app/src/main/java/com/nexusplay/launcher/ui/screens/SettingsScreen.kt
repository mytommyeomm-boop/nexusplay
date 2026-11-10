package com.nexusplay.launcher.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.nexusplay.launcher.core.DeviceInfo
import com.nexusplay.launcher.core.DeviceInfoReader
import com.nexusplay.launcher.core.LocalDeviceInfo
import com.nexusplay.launcher.ui.components.ChamferShape
import com.nexusplay.launcher.ui.components.DeckPanel
import com.nexusplay.launcher.ui.components.NeonIcons
import com.nexusplay.launcher.ui.components.Readout
import com.nexusplay.launcher.ui.components.ScreenHeader
import com.nexusplay.launcher.ui.components.ScreenList
import com.nexusplay.launcher.ui.components.SectionTitle
import com.nexusplay.launcher.ui.components.SegmentBar
import com.nexusplay.launcher.ui.components.StatusLed
import com.nexusplay.launcher.ui.theme.Cyan
import com.nexusplay.launcher.ui.theme.Graphite0
import com.nexusplay.launcher.ui.theme.Lime
import com.nexusplay.launcher.ui.theme.Orange
import com.nexusplay.launcher.ui.theme.TextMuted
import com.nexusplay.launcher.ui.theme.Titanium

@Suppress("DEPRECATION")
private fun appVersion(context: Context): String =
    runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
        .getOrNull() ?: "unknown"

private data class PreferenceEntry(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val onClick: (() -> Unit)? = null
)

private val plannedPreferences = listOf(
    PreferenceEntry(NeonIcons.Cpu, "Graphics and performance", "Resolution scale, frame limit, power use"),
    PreferenceEntry(NeonIcons.Disk, "Storage", "Where games live and how much space they use"),
    PreferenceEntry(NeonIcons.Layers, "Interface", "Accent colors and layout density"),
    PreferenceEntry(NeonIcons.Backup, "Backup and restore", "Export and import your library and settings"),
    PreferenceEntry(NeonIcons.Key, "Owner mode", "App name, logo and administration")
)

@Composable
fun SettingsScreen(
    contentPadding: PaddingValues,
    onOpenJoystick: () -> Unit,
    onOpenRuntimes: () -> Unit
) {
    val device = LocalDeviceInfo.current
    val context = LocalContext.current
    val version = remember { appVersion(context) }

    ScreenList(contentPadding) {
        item { ScreenHeader("Settings", "Device facts and app preferences", readout = "v$version") }
        item { DevicePanel(device) }
        item { SectionTitle("Preferences") }
        item {
            PreferencePanel(
                listOf(
                    PreferenceEntry(
                        NeonIcons.Joystick, "Joystick",
                        "Sensitivity, deadzone, look and calibration", onOpenJoystick
                    ),
                    PreferenceEntry(
                        NeonIcons.Bolt, "Runtimes",
                        "Detect, select and test PC game runtimes", onOpenRuntimes
                    )
                ) + plannedPreferences
            )
        }
    }
}

@Composable
private fun DevicePanel(info: DeviceInfo) {
    DeckPanel(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = ChamferShape(22.dp, topRight = true, bottomLeft = true),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("This device", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            StatusLed(if (info.is64BitArm) Lime else Orange)
            Spacer(Modifier.width(8.dp))
            Text(
                if (info.is64BitArm) "ARM64" else "Not ARM64",
                style = MaterialTheme.typography.labelSmall,
                color = if (info.is64BitArm) Lime else Orange
            )
        }
        Spacer(Modifier.height(8.dp))
        Readout("Device", info.model)
        Readout("System", info.androidVersion)
        Readout("CPU", info.primaryAbi)
        Readout("Memory", DeviceInfoReader.formatGb(info.totalRamBytes))
        Readout("Vulkan", if (info.hasVulkan) "Reported by device" else "Not reported")
        Readout(
            "Free storage",
            "${DeviceInfoReader.formatGb(info.freeStorageBytes)} of ${DeviceInfoReader.formatGb(info.totalStorageBytes)}",
            valueColor = Cyan
        )
        Spacer(Modifier.height(6.dp))
        SegmentBar(fraction = { info.storageUsedFraction }, color = Cyan)
    }
}

/** One continuous panel with hairline dividers instead of a card per row. */
@Composable
private fun PreferencePanel(entries: List<PreferenceEntry>) {
    DeckPanel(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = ChamferShape(16.dp, topLeft = true),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
    ) {
        entries.forEachIndexed { index, entry ->
            if (index > 0) {
                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Titanium.copy(alpha = 0.12f))
                )
            }
            PreferenceRow(entry)
        }
    }
}

@Composable
private fun PreferenceRow(entry: PreferenceEntry) {
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .then(
                if (entry.onClick != null) {
                    Modifier.clickable(
                        interactionSource = interaction,
                        indication = null,
                        role = Role.Button,
                        onClick = entry.onClick
                    )
                } else Modifier
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(ChamferShape(9.dp, topLeft = true))
                .background(Graphite0.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(entry.icon, contentDescription = null, tint = Titanium, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(entry.title, style = MaterialTheme.typography.titleSmall)
            Text(entry.subtitle, style = MaterialTheme.typography.bodySmall, color = Titanium)
        }
        Spacer(Modifier.width(10.dp))
        val live = entry.onClick != null
        StatusLed(Lime, lit = live)
        Spacer(Modifier.width(6.dp))
        Text(
            if (live) "open" else "soon",
            style = MaterialTheme.typography.labelSmall,
            color = if (live) Lime else TextMuted
        )
    }
}
