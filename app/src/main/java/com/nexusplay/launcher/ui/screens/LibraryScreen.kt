package com.nexusplay.launcher.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.nexusplay.launcher.R
import com.nexusplay.launcher.ui.components.EmptyState
import com.nexusplay.launcher.ui.components.NeonIcons
import com.nexusplay.launcher.ui.components.ScreenHeader
import com.nexusplay.launcher.ui.components.ScreenList
import com.nexusplay.launcher.ui.navigation.Destination

@Composable
fun LibraryScreen(contentPadding: PaddingValues, onNavigate: (Destination) -> Unit) {
    ScreenList(contentPadding) {
        item { ScreenHeader(stringResource(R.string.library_title), stringResource(R.string.library_subtitle)) }
        item {
            EmptyState(
                icon = NeonIcons.Library,
                title = stringResource(R.string.library_empty_title),
                message = stringResource(R.string.library_empty_message),
                actionLabel = stringResource(R.string.action_import_game),
                onAction = { onNavigate(Destination.Import) }
            )
        }
    }
}
