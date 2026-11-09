package com.nexusplay.launcher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexusplay.launcher.ui.components.ChamferShape
import com.nexusplay.launcher.ui.components.Corner
import com.nexusplay.launcher.ui.components.DeckButton
import com.nexusplay.launcher.ui.components.DeckPanel
import com.nexusplay.launcher.ui.components.NeonIcons
import com.nexusplay.launcher.ui.components.ScreenHeader
import com.nexusplay.launcher.ui.components.ScreenList
import com.nexusplay.launcher.ui.components.SectionTitle
import com.nexusplay.launcher.ui.components.SlotOutline
import com.nexusplay.launcher.ui.components.StatusLed
import com.nexusplay.launcher.ui.theme.Graphite0
import com.nexusplay.launcher.ui.theme.Graphite3
import com.nexusplay.launcher.ui.theme.Lime
import com.nexusplay.launcher.ui.theme.Orange
import com.nexusplay.launcher.ui.theme.TextMuted
import com.nexusplay.launcher.ui.theme.Titanium

@Composable
fun ImportScreen(contentPadding: PaddingValues) {
    ScreenList(contentPadding) {
        item { ScreenHeader("Import", "Bring a game archive into your library") }
        item { IntakePlate() }
        item { Formats() }
        item { Steps() }
    }
}

@Composable
private fun IntakePlate() {
    DeckPanel(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = ChamferShape(24.dp, topLeft = true, bottomRight = true),
        screws = setOf(Corner.TopRight, Corner.BottomLeft),
        contentPadding = PaddingValues(18.dp)
    ) {
        SlotOutline(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp),
            icon = NeonIcons.Import,
            pins = true
        )
        Spacer(Modifier.height(16.dp))
        Text("Select a game archive", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text(
            "Choose a ZIP, 7Z or TAR file from your phone. Nothing is run during import.",
            style = MaterialTheme.typography.bodyMedium,
            color = Titanium
        )
        Spacer(Modifier.height(18.dp))
        // Disabled on purpose: the file picker and extractor are not built yet.
        DeckButton(text = "Choose file", onClick = {}, icon = NeonIcons.Folder, enabled = false)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusLed(Orange)
            Spacer(Modifier.width(10.dp))
            Text(
                "The file picker is not built yet. It arrives with the extraction update.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun Formats() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Formats")
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FormatKey("ZIP", planned = true)
            FormatKey("7Z", planned = true)
            FormatKey("TAR", planned = true)
            FormatKey("RAR", planned = false)
        }
        Text(
            "Lit formats arrive with the extraction update. RAR needs a separate decoder and comes later.",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
    }
}

@Composable
private fun FormatKey(label: String, planned: Boolean) {
    val shape = ChamferShape(7.dp, topLeft = true)
    Row(
        modifier = Modifier
            .clip(shape)
            .background(Graphite0.copy(alpha = 0.7f))
            .border(1.dp, Graphite3, shape)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatusLed(Lime, lit = planned)
        Spacer(Modifier.width(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp),
            color = if (planned) Color.White else TextMuted
        )
    }
}

/** A real sequence, so it is drawn as stations on a line. */
@Composable
private fun Steps() {
    val steps = listOf(
        "Choose an archive from your storage",
        "Pick where the game files are extracted",
        "Watch progress, or cancel at any time",
        "Review the detected executable and confirm"
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Import steps")
        Column(Modifier.padding(horizontal = 20.dp)) {
            steps.forEachIndexed { index, text ->
                val last = index == steps.lastIndex
                Row(Modifier.height(IntrinsicSize.Min)) {
                    Box(
                        Modifier
                            .width(28.dp)
                            .fillMaxHeight()
                            .drawBehind {
                                val cx = 8.dp.toPx()
                                val top = 11.dp.toPx()
                                val half = 4.5.dp.toPx()
                                drawRect(
                                    Lime,
                                    Offset(cx - half, top - half),
                                    Size(half * 2, half * 2),
                                    style = Stroke(1.5.dp.toPx())
                                )
                                if (!last) {
                                    drawLine(
                                        Titanium.copy(alpha = 0.35f),
                                        Offset(cx, top + half + 3.dp.toPx()),
                                        Offset(cx, size.height),
                                        1.5.dp.toPx()
                                    )
                                }
                            }
                    )
                    Text(
                        text,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = if (last) 0.dp else 20.dp)
                    )
                }
            }
        }
    }
}
