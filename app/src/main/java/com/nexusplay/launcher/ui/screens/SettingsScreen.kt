package com.nexusplay.launcher.ui.screens

import com.nexusplay.launcher.ui.mascot.tutorialAnchor
import com.nexusplay.launcher.mascot.Anchor
import android.content.Context
import androidx.annotation.StringRes
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.nexusplay.launcher.R
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
        .getOrNull() ?: context.getString(R.string.unknown)

private data class PreferenceEntry(
    val icon: ImageVector,
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    val onClick: (() -> Unit)? = null
)

private val plannedPreferences = listOf(
    PreferenceEntry(NeonIcons.Cpu, R.string.pref_graphics_title, R.string.pref_graphics_subtitle),
    PreferenceEntry(NeonIcons.Disk, R.string.pref_storage_title, R.string.pref_storage_subtitle),
    PreferenceEntry(NeonIcons.Layers, R.string.pref_interface_title, R.string.pref_interface_subtitle),
    PreferenceEntry(NeonIcons.Backup, R.string.pref_backup_title, R.string.pref_backup_subtitle),
    PreferenceEntry(NeonIcons.Key, R.string.pref_owner_title, R.string.pref_owner_subtitle)
)

@Composable
fun SettingsScreen(
    contentPadding: PaddingValues,
    onOpenLanguage: () -> Unit,
    onOpenMascot: () -> Unit,
    onOpenJoystick: () -> Unit,
    onOpenRuntimes: () -> Unit
) {
    val device = LocalDeviceInfo.current
    val context = LocalContext.current
    val version = remember { appVersion(context) }

    ScreenList(contentPadding) {
        item {
            ScreenHeader(
                stringResource(R.string.settings_title),
                stringResource(R.string.settings_subtitle),
                readout = "v$version"
            )
        }
        item { DevicePanel(device) }
        item { SectionTitle(stringResource(R.string.settings_prefs_title)) }
        item {
            PreferencePanel(
                listOf(
                    PreferenceEntry(NeonIcons.Globe, R.string.pref_language_title, R.string.pref_language_subtitle, onOpenLanguage),
                    PreferenceEntry(NeonIcons.Mascot, R.string.pref_mascot_title, R.string.pref_mascot_subtitle, onOpenMascot),
                    PreferenceEntry(NeonIcons.Joystick, R.string.pref_joystick_title, R.string.pref_joystick_subtitle, onOpenJoystick),
                    PreferenceEntry(NeonIcons.Bolt, R.string.pref_runtimes_title, R.string.pref_runtimes_subtitle, onOpenRuntimes)
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
            Text(
                stringResource(R.string.settings_device_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            StatusLed(if (info.is64BitArm) Lime else Orange)
            Spacer(Modifier.width(8.dp))
            Text(
                if (info.is64BitArm) "ARM64" else stringResource(R.string.settings_not_arm64),
                style = MaterialTheme.typography.labelSmall,
                color = if (info.is64BitArm) Lime else Orange
            )
        }
        Spacer(Modifier.height(8.dp))
        Readout(stringResource(R.string.settings_row_device), info.model)
        Readout(stringResource(R.string.settings_row_system), info.androidVersion)
        Readout(stringResource(R.string.settings_row_cpu), info.primaryAbi)
        Readout(stringResource(R.string.settings_row_memory), DeviceInfoReader.formatGb(info.totalRamBytes))
        Readout(
            stringResource(R.string.vulkan_label),
            stringResource(if (info.hasVulkan) R.string.settings_vulkan_yes else R.string.settings_vulkan_no)
        )
        Readout(
            stringResource(R.string.settings_row_free_storage),
            stringResource(
                R.string.home_storage_value,
                DeviceInfoReader.formatGb(info.freeStorageBytes),
                DeviceInfoReader.formatGb(info.totalStorageBytes)
            ),
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
            .padding(horizontal = 20.dp)
            .tutorialAnchor(Anchor.SettingsPrefs),
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
            Text(stringResource(entry.titleRes), style = MaterialTheme.typography.titleSmall)
            Text(stringResource(entry.subtitleRes), style = MaterialTheme.typography.bodySmall, color = Titanium)
        }
        Spacer(Modifier.width(10.dp))
        val live = entry.onClick != null
        StatusLed(Lime, lit = live)
        Spacer(Modifier.width(6.dp))
        Text(
            stringResource(if (live) R.string.pref_tag_open else R.string.pref_tag_soon),
            style = MaterialTheme.typography.labelSmall,
            color = if (live) Lime else TextMuted
        )
    }
}
