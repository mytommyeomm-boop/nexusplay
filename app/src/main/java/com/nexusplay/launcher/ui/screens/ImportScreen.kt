package com.nexusplay.launcher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nexusplay.launcher.ui.components.GlassCard
import com.nexusplay.launcher.ui.components.NeonButton
import com.nexusplay.launcher.ui.components.NeonIcons
import com.nexusplay.launcher.ui.components.NeonOrb
import com.nexusplay.launcher.ui.components.ScreenHeader
import com.nexusplay.launcher.ui.components.ScreenList
import com.nexusplay.launcher.ui.components.SectionTitle
import com.nexusplay.launcher.ui.components.StatusTag
import com.nexusplay.launcher.ui.components.staggeredEntrance
import com.nexusplay.launcher.ui.theme.NeonCyan
import com.nexusplay.launcher.ui.theme.NeonGradient
import com.nexusplay.launcher.ui.theme.NeonPurple
import com.nexusplay.launcher.ui.theme.TextMuted
import com.nexusplay.launcher.ui.theme.TextSecondary

@Composable
fun ImportScreen(contentPadding: PaddingValues) {
    ScreenList(contentPadding) {
        item { ScreenHeader("Import", "Bring a game archive into your library") }
        item { Box(Modifier.staggeredEntrance(1)) { DropZone() } }
        item { Box(Modifier.staggeredEntrance(2)) { Formats() } }
        item { Box(Modifier.staggeredEntrance(3)) { Steps() } }
    }
}

@Composable
private fun DropZone() {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        accent = NeonPurple
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
            NeonOrb(NeonIcons.Import)
            Spacer(Modifier.height(14.dp))
            Text("Select a game archive", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                "Pick a ZIP, 7Z or TAR file from your phone. Nothing is run during import.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
            // Disabled on purpose: the file picker and extractor are the next stage.
            NeonButton(text = "Choose file", onClick = {}, icon = NeonIcons.Folder, enabled = false)
            Spacer(Modifier.height(10.dp))
            StatusTag("Available after the next update", NeonPurple)
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun Formats() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle("Supported formats")
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusTag("ZIP", NeonCyan)
            StatusTag("7Z", NeonCyan)
            StatusTag("TAR", NeonCyan)
            StatusTag("RAR (later)", TextMuted)
        }
        Text(
            "RAR needs a separate decoder, so it is planned for a later version.",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
    }
}

@Composable
private fun Steps() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle("How importing will work")
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            val steps = listOf(
                "Choose an archive from your storage",
                "Pick where the game files are extracted",
                "Watch progress, or cancel at any time",
                "Review the detected executable and confirm"
            )
            steps.forEachIndexed { index, text ->
                if (index > 0) Spacer(Modifier.height(14.dp))
                StepRow(index + 1, text)
            }
        }
    }
}

@Composable
private fun StepRow(number: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(NeonCyan.copy(alpha = 0.08f))
                .border(1.dp, NeonGradient, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(number.toString(), style = MaterialTheme.typography.labelSmall, color = NeonCyan)
        }
        Spacer(Modifier.width(14.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}
