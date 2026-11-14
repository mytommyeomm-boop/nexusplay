package com.nexusplay.launcher.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nexusplay.launcher.R
import com.nexusplay.launcher.mascot.LocalMascot
import com.nexusplay.launcher.mascot.MascotAsset
import com.nexusplay.launcher.mascot.MascotCatalog
import com.nexusplay.launcher.mascot.MascotCategory
import com.nexusplay.launcher.mascot.ReviewState
import com.nexusplay.launcher.ui.components.ChamferShape
import com.nexusplay.launcher.ui.components.DeckPanel
import com.nexusplay.launcher.ui.components.KeyButton
import com.nexusplay.launcher.ui.mascot.AssetImage
import com.nexusplay.launcher.ui.theme.Cyan
import com.nexusplay.launcher.ui.theme.Lime
import com.nexusplay.launcher.ui.theme.Orange
import com.nexusplay.launcher.ui.theme.Titanium

/** Every pose with its real name. Normal users see confirmed art that fits their language. */
@Composable
fun MascotGalleryScreen(contentPadding: PaddingValues, onBack: () -> Unit) {
    val mascot = LocalMascot.current
    val language = mascot.language
    var filter by remember { mutableStateOf<MascotCategory?>(null) }
    var selected by remember { mutableStateOf<MascotAsset?>(null) }

    val pool = remember(language, mascot.developerMode) {
        MascotCatalog.all.filter { mascot.developerMode || (it.review == ReviewState.Confirmed && it.usableIn(language)) }
    }
    val visible = remember(pool, filter) {
        val f = filter
        if (f == null) pool else pool.filter { f in it.categories }
    }
    val chips = remember(pool) { MascotCategory.entries.filter { c -> pool.any { c in it.categories } } }
    val current = selected?.takeIf { it in visible } ?: visible.firstOrNull()

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp, end = 20.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 20.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                KeyButton(stringResource(R.string.action_back), onBack)
                Spacer(Modifier.width(14.dp))
                Text(stringResource(R.string.gallery_title), style = MaterialTheme.typography.headlineMedium)
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(stringResource(R.string.gallery_subtitle), style = MaterialTheme.typography.bodyMedium, color = Titanium)
        }
        if (current != null) item(span = { GridItemSpan(maxLineSpan) }) { DetailCard(current) }
        item(span = { GridItemSpan(maxLineSpan) }) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { KeyButton(stringResource(R.string.gallery_all), { filter = null }, highlight = filter == null) }
                items(chips) { c -> KeyButton(stringResource(c.labelRes), { filter = c }, highlight = filter == c) }
            }
        }
        items(visible, key = { it.id }) { asset ->
            Tile(asset, selected = asset == current) { selected = asset }
        }
    }
}

@Composable
private fun DetailCard(asset: MascotAsset) {
    val categories = asset.categories.map { stringResource(it.labelRes) }.joinToString(", ")
    DeckPanel(
        modifier = Modifier.fillMaxWidth(),
        shape = ChamferShape(20.dp, topLeft = true, bottomRight = true),
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(170.dp)) { AssetImage(asset) }
            Spacer(Modifier.width(12.dp))
            androidx.compose.foundation.layout.Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(asset.nameRes), style = MaterialTheme.typography.titleMedium, color = Lime)
                Text("${stringResource(R.string.gallery_categories)}: $categories", style = MaterialTheme.typography.bodySmall, color = Titanium)
                Text(stringResource(asset.mouth.labelRes), style = MaterialTheme.typography.labelSmall, color = Cyan)
                if (asset.language != null) {
                    Text(stringResource(R.string.gallery_thai_note), style = MaterialTheme.typography.bodySmall, color = Orange)
                }
                if (asset.review == ReviewState.NeedsReview) {
                    Text(stringResource(R.string.lab_needs_review), style = MaterialTheme.typography.labelSmall, color = Orange)
                }
            }
        }
    }
}

@Composable
private fun Tile(asset: MascotAsset, selected: Boolean, onClick: () -> Unit) {
    DeckPanel(
        modifier = Modifier.fillMaxWidth(),
        shape = ChamferShape(10.dp, topLeft = true),
        onClick = onClick,
        contentPadding = PaddingValues(6.dp)
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(1f)) { AssetImage(asset) }
        Text(
            stringResource(asset.nameRes),
            style = MaterialTheme.typography.bodySmall,
            color = if (selected) Lime else Titanium,
            maxLines = 3,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
        )
    }
}
