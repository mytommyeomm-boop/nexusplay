package com.nexusplay.launcher.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.nexusplay.launcher.ui.components.EmptyState
import com.nexusplay.launcher.ui.components.NeonIcons
import com.nexusplay.launcher.ui.components.ScreenHeader
import com.nexusplay.launcher.ui.components.ScreenList
import com.nexusplay.launcher.ui.navigation.Destination

@Composable
fun LibraryScreen(contentPadding: PaddingValues, onNavigate: (Destination) -> Unit) {
    ScreenList(contentPadding) {
        item { ScreenHeader("Library", "Every game you have imported") }
        item {
            EmptyState(
                icon = NeonIcons.Library,
                title = "Your library is empty",
                message = "Imported games appear here as cover cards you can open, favorite and launch.",
                actionLabel = "Import a game",
                onAction = { onNavigate(Destination.Import) }
            )
        }
    }
}
