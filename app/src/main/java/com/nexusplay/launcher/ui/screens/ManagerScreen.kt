package com.nexusplay.launcher.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.nexusplay.launcher.ui.components.EmptyState
import com.nexusplay.launcher.ui.components.NeonIcons
import com.nexusplay.launcher.ui.components.ScreenHeader
import com.nexusplay.launcher.ui.components.ScreenList
import com.nexusplay.launcher.ui.navigation.Destination

@Composable
fun ManagerScreen(contentPadding: PaddingValues, onNavigate: (Destination) -> Unit) {
    ScreenList(contentPadding) {
        item { ScreenHeader("Manager", "Rename, re-cover and organize games") }
        item {
            EmptyState(
                icon = NeonIcons.Folder,
                title = "Nothing to manage yet",
                message = "Once you import a game you can rename it, change its cover and manage its folders here.",
                actionLabel = "Import a game",
                onAction = { onNavigate(Destination.Import) }
            )
        }
    }
}
