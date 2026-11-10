package com.nexusplay.launcher.ui.screens

import androidx.compose.ui.res.stringResource
import com.nexusplay.launcher.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.nexusplay.launcher.core.DeviceInfo
import com.nexusplay.launcher.core.DeviceInfoReader
import com.nexusplay.launcher.core.LocalDeviceInfo
import com.nexusplay.launcher.ui.components.ChamferShape
import com.nexusplay.launcher.ui.components.Corner
import com.nexusplay.launcher.ui.components.DeckButton
import com.nexusplay.launcher.ui.components.DeckPanel
import com.nexusplay.launcher.ui.components.LogoMark
import com.nexusplay.launcher.ui.components.NeonIcons
import com.nexusplay.launcher.ui.components.Readout
import com.nexusplay.launcher.ui.components.ScreenList
import com.nexusplay.launcher.ui.components.SectionTitle
import com.nexusplay.launcher.ui.components.SegmentBar
import com.nexusplay.launcher.ui.components.SlotOutline
import com.nexusplay.launcher.ui.components.StatusLed
import com.nexusplay.launcher.ui.navigation.Destination
import com.nexusplay.launcher.ui.theme.Cyan
import com.nexusplay.launcher.ui.theme.Lime
import com.nexusplay.launcher.ui.theme.Orange
import com.nexusplay.launcher.ui.theme.TextMuted
import com.nexusplay.launcher.ui.theme.Titanium

@Composable
fun HomeScreen(contentPadding: PaddingValues, onNavigate: (Destination) -> Unit) {
    val device = LocalDeviceInfo.current
    ScreenList(contentPadding) {
        item { HomeHeader(device) }
        item { HeroPlate(device) { onNavigate(Destination.Import) } }
        item { ModuleGrid(onNavigate) }
        item { SlotShelf(stringResource(R.string.shelf_recent_title), stringResource(R.string.shelf_recent_hint)) }
        item { SlotShelf(stringResource(R.string.shelf_favorites_title), stringResource(R.string.shelf_favorites_hint)) }
        item { SlotShelf(stringResource(R.string.shelf_new_title), stringResource(R.string.shelf_new_hint)) }
    }
}

@Composable
private fun HomeHeader(device: DeviceInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LogoMark(46.dp)
        Spacer(Modifier.width(14.dp))
        Column {
            Text("NexusPlay", style = MaterialTheme.typography.headlineMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusLed(if (device.is64BitArm) Lime else Orange)
                Spacer(Modifier.width(8.dp))
                Text(device.primaryAbi, style = MaterialTheme.typography.labelSmall, color = Cyan)
            }
        }
    }
}

@Composable
private fun HeroPlate(device: DeviceInfo, onImport: () -> Unit) {
    DeckPanel(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = ChamferShape(26.dp, topRight = true, bottomLeft = true),
        screws = setOf(Corner.TopLeft, Corner.BottomRight),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 20.dp)
    ) {
        Text(stringResource(R.string.home_hero_title), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(6.dp))
        Text(
            stringResource(R.string.home_hero_body),
            style = MaterialTheme.typography.bodyMedium,
            color = Titanium
        )
        Spacer(Modifier.height(16.dp))
        Readout(
            label = stringResource(R.string.home_storage_label),
            value = stringResource(
                R.string.home_storage_value,
                DeviceInfoReader.formatGb(device.freeStorageBytes),
                DeviceInfoReader.formatGb(device.totalStorageBytes)
            )
        )
        SegmentBar(fraction = { device.storageUsedFraction }, color = Cyan)
        Spacer(Modifier.height(20.dp))
        DeckButton(text = stringResource(R.string.action_import_game), onClick = onImport, icon = NeonIcons.Import)
    }
}

/** One tall module beside two short ones, so the grid is deliberately uneven. */
@Composable
private fun ModuleGrid(onNavigate: (Destination) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(232.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Module(
            icon = NeonIcons.Library,
            title = stringResource(R.string.nav_library),
            caption = stringResource(R.string.home_module_library_caption),
            iconTint = Lime,
            shape = ChamferShape(22.dp, topLeft = true, bottomRight = true),
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight()
        ) { onNavigate(Destination.Library) }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Module(
                icon = NeonIcons.Folder,
                title = stringResource(R.string.nav_manager),
                caption = stringResource(R.string.home_module_manager_caption),
                iconTint = Titanium,
                shape = ChamferShape(14.dp, topRight = true),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { onNavigate(Destination.Manager) }
            Module(
                icon = NeonIcons.Sliders,
                title = stringResource(R.string.nav_settings),
                caption = stringResource(R.string.home_module_settings_caption),
                iconTint = Titanium,
                shape = ChamferShape(14.dp, bottomLeft = true),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { onNavigate(Destination.Settings) }
        }
    }
}

@Composable
private fun Module(
    icon: ImageVector,
    title: String,
    caption: String,
    iconTint: Color,
    shape: ChamferShape,
    modifier: Modifier,
    onClick: () -> Unit
) {
    DeckPanel(
        modifier = modifier,
        shape = shape,
        onClick = onClick,
        contentPadding = PaddingValues(14.dp)
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(26.dp))
        Spacer(Modifier.weight(1f))
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(caption, style = MaterialTheme.typography.bodySmall, color = Titanium)
    }
}

/** Four empty slots in a row: a shelf that is waiting for games rather than a blank card. */
@Composable
private fun SlotShelf(title: String, hint: String) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle(title)
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            repeat(4) {
                SlotOutline(
                    Modifier
                        .weight(1f)
                        .aspectRatio(0.85f)
                )
            }
        }
        Text(
            hint,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
    }
}
