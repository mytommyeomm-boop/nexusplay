package com.nexusplay.launcher.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.nexusplay.launcher.ui.components.GlassCard
import com.nexusplay.launcher.ui.components.GradientTitle
import com.nexusplay.launcher.ui.components.LogoMark
import com.nexusplay.launcher.ui.components.NeonButton
import com.nexusplay.launcher.ui.components.NeonIcons
import com.nexusplay.launcher.ui.components.ScreenList
import com.nexusplay.launcher.ui.components.SectionTitle
import com.nexusplay.launcher.ui.components.staggeredEntrance
import com.nexusplay.launcher.ui.navigation.Destination
import com.nexusplay.launcher.ui.theme.NeonCyan
import com.nexusplay.launcher.ui.theme.NeonPurple
import com.nexusplay.launcher.ui.theme.TextSecondary

@Composable
fun HomeScreen(contentPadding: PaddingValues, onNavigate: (Destination) -> Unit) {
    ScreenList(contentPadding) {
        item { Box(Modifier.staggeredEntrance(0)) { HomeHeader() } }
        item { Box(Modifier.staggeredEntrance(1)) { HeroCard { onNavigate(Destination.Import) } } }
        item { Box(Modifier.staggeredEntrance(2)) { QuickActions(onNavigate) } }
        item { Box(Modifier.staggeredEntrance(3)) { Shelf("Recently played", "Games you launch will show up here.") } }
        item { Box(Modifier.staggeredEntrance(4)) { Shelf("Favorites", "Games you mark as favorite will be pinned here.") } }
        item { Box(Modifier.staggeredEntrance(5)) { Shelf("Recently imported", "New imports will appear here.") } }
    }
}

@Composable
private fun HomeHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LogoMark(46.dp)
        Spacer(Modifier.width(14.dp))
        Column {
            Text("Welcome back", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            GradientTitle("NexusPlay")
        }
    }
}

@Composable
private fun HeroCard(onImport: () -> Unit) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        accent = NeonPurple
    ) {
        Text("Build your library", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "Import a game archive, check the detected executable, then launch it through a compatible runtime.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Spacer(Modifier.height(18.dp))
        NeonButton(
            text = "Import a game",
            onClick = onImport,
            icon = NeonIcons.Import,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun QuickActions(onNavigate: (Destination) -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickTile(NeonIcons.Import, "Import", "Add a game", Modifier.weight(1f)) { onNavigate(Destination.Import) }
            QuickTile(NeonIcons.Library, "Library", "Browse games", Modifier.weight(1f)) { onNavigate(Destination.Library) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickTile(NeonIcons.Folder, "Manager", "Files and covers", Modifier.weight(1f)) { onNavigate(Destination.Manager) }
            QuickTile(NeonIcons.Sliders, "Settings", "Device and runtime", Modifier.weight(1f)) { onNavigate(Destination.Settings) }
        }
    }
}

@Composable
private fun QuickTile(
    icon: ImageVector,
    title: String,
    caption: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    GlassCard(modifier = modifier.height(112.dp), onClick = onClick) {
        Icon(icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(26.dp))
        Spacer(Modifier.weight(1f))
        Text(title, style = MaterialTheme.typography.titleSmall)
        Text(caption, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}

@Composable
private fun Shelf(title: String, hint: String) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle(title)
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(hint, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
    }
}
