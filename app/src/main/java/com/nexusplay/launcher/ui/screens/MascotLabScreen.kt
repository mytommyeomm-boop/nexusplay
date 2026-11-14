package com.nexusplay.launcher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexusplay.launcher.R
import com.nexusplay.launcher.core.UiText
import com.nexusplay.launcher.core.text
import com.nexusplay.launcher.mascot.LocalMascot
import com.nexusplay.launcher.mascot.MascotAsset
import com.nexusplay.launcher.mascot.MascotCatalog
import com.nexusplay.launcher.mascot.MascotEvent
import com.nexusplay.launcher.mascot.MascotIntegrity
import com.nexusplay.launcher.mascot.MascotNotice
import com.nexusplay.launcher.mascot.NoticeKind
import com.nexusplay.launcher.mascot.ReviewState
import com.nexusplay.launcher.mascot.Tutorials
import com.nexusplay.launcher.ui.components.ChamferShape
import com.nexusplay.launcher.ui.components.DeckPanel
import com.nexusplay.launcher.ui.components.KeyButton
import com.nexusplay.launcher.ui.components.SectionTitle
import com.nexusplay.launcher.ui.graphics.checkerBackground
import com.nexusplay.launcher.ui.mascot.AssetImage
import com.nexusplay.launcher.ui.theme.Cyan
import com.nexusplay.launcher.ui.theme.Graphite3
import com.nexusplay.launcher.ui.theme.Lime
import com.nexusplay.launcher.ui.theme.Orange
import com.nexusplay.launcher.ui.theme.Titanium
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private fun kindLabel(k: NoticeKind): Int = when (k) {
    NoticeKind.Success -> R.string.notice_kind_success
    NoticeKind.Warning -> R.string.notice_kind_warning
    NoticeKind.Error -> R.string.notice_kind_error
    NoticeKind.Loading -> R.string.notice_kind_loading
    NoticeKind.Info -> R.string.notice_kind_info
    NoticeKind.MissingRuntime -> R.string.notice_kind_missingruntime
    NoticeKind.LaunchIssue -> R.string.notice_kind_launchissue
    NoticeKind.GamepadSaved -> R.string.notice_kind_gamepadsaved
    NoticeKind.Tip -> R.string.notice_kind_tip
}

/** Developer-only: verify every image, every event-to-pose mapping and every notification type. */
@Composable
fun MascotLabScreen(contentPadding: PaddingValues, onBack: () -> Unit) {
    val mascot = LocalMascot.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var result by remember { mutableStateOf<MascotIntegrity.Result?>(null) }
    var selectedEvent by remember { mutableStateOf<MascotEvent?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp, end = 20.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                KeyButton(stringResource(R.string.action_back), onBack)
                Spacer(Modifier.width(14.dp))
                Text(stringResource(R.string.lab_title), style = MaterialTheme.typography.headlineMedium)
            }
        }
        item { Text(stringResource(R.string.lab_subtitle), style = MaterialTheme.typography.bodyMedium, color = Titanium) }

        item { Header(stringResource(R.string.lab_section_integrity)) }
        item {
            DeckPanel(Modifier.fillMaxWidth(), shape = ChamferShape(14.dp, topLeft = true), contentPadding = PaddingValues(14.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    KeyButton(
                        stringResource(R.string.lab_run_check),
                        { scope.launch(Dispatchers.Default) { result = MascotIntegrity.run(context, mascot.language, mascot.poseOverrides) } },
                        Modifier.fillMaxWidth()
                    )
                    result?.let { r ->
                        if (r.issues.isEmpty()) {
                            Text(stringResource(R.string.lab_check_ok, r.assets, r.events), style = MaterialTheme.typography.bodyMedium, color = Lime)
                        } else {
                            Text(stringResource(R.string.lab_check_problems, r.issues.size), style = MaterialTheme.typography.titleSmall, color = Orange)
                            r.issues.forEach { Text(it.text(), style = MaterialTheme.typography.bodySmall, color = Orange) }
                        }
                    }
                }
            }
        }

        item { Header(stringResource(R.string.lab_section_notices)) }
        items(NoticeKind.entries) { kind ->
            KeyButton(
                stringResource(kindLabel(kind)),
                {
                    mascot.notify(
                        MascotNotice(
                            "lab_${kind.name}", kind,
                            UiText(R.string.lab_test_title, UiText(kindLabel(kind))), UiText(R.string.lab_test_message)
                        ),
                        cooldownMs = 0L, force = true
                    )
                },
                Modifier.fillMaxWidth()
            )
        }

        item { Header(stringResource(R.string.lab_section_flows)) }
        item { KeyButton(stringResource(R.string.lab_test_welcome), { mascot.showWelcomeAgain() }, Modifier.fillMaxWidth()) }
        items(Tutorials.all) { t ->
            KeyButton(stringResource(R.string.lab_test_tutorial, stringResource(t.nameRes)), { mascot.requestTutorial(t) }, Modifier.fillMaxWidth())
        }

        item { Header(stringResource(R.string.lab_section_events)) }
        item { KeyButton(stringResource(R.string.lab_reset_all), { mascot.clearPoseOverrides() }, Modifier.fillMaxWidth(), enabled = mascot.poseOverrides.isNotEmpty()) }
        items(MascotEvent.entries) { event ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EventRow(event, selectedEvent == event) { selectedEvent = if (selectedEvent == event) null else event }
                if (selectedEvent == event) Picker(event)
            }
        }

        item { Header(stringResource(R.string.lab_section_assets)) }
        items(MascotCatalog.all, key = { it.id }) { AssetRow(it) }

        item {
            KeyButton(stringResource(R.string.lab_disable_dev), { mascot.setDeveloperMode(false); onBack() }, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun Header(title: String) {
    Column(Modifier.padding(top = 10.dp)) { SectionTitleFlat(title) }
}

@Composable
private fun SectionTitleFlat(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, color = Lime)
}

@Composable
private fun EventRow(event: MascotEvent, open: Boolean, onToggle: () -> Unit) {
    val mascot = LocalMascot.current
    val asset = mascot.assetFor(event)
    val interaction = remember { MutableInteractionSource() }
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onToggle),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(64.dp).checkerBackground()) { AssetImage(asset) }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(event.name, style = MaterialTheme.typography.titleSmall)
            Text("${event.expression.name} > ${asset.id}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = Cyan)
            if (!event.wired) Text(stringResource(R.string.lab_not_wired), style = MaterialTheme.typography.bodySmall, color = Orange)
            if (event.name in mascot.poseOverrides) Text(stringResource(R.string.lab_overridden), style = MaterialTheme.typography.bodySmall, color = Lime)
        }
        KeyButton(stringResource(if (open) R.string.action_close else R.string.lab_change), onToggle)
    }
}

@Composable
private fun Picker(event: MascotEvent) {
    val mascot = LocalMascot.current
    val current = mascot.assetFor(event)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.lab_pick), style = MaterialTheme.typography.bodySmall, color = Titanium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(MascotCatalog.all, key = { it.id }) { a ->
                val interaction = remember { MutableInteractionSource() }
                Box(
                    Modifier
                        .size(84.dp)
                        .checkerBackground()
                        .border(if (a.id == current.id) 2.dp else 1.dp, if (a.id == current.id) Lime else Graphite3)
                        .clickable(interactionSource = interaction, indication = null, role = Role.Button) {
                            mascot.setPoseOverride(event, a.id)
                        }
                ) { AssetImage(a) }
            }
        }
        KeyButton(stringResource(R.string.lab_reset), { mascot.setPoseOverride(event, null) }, enabled = event.name in mascot.poseOverrides)
    }
}

@Composable
private fun AssetRow(a: MascotAsset) {
    val cats = a.categories.map { stringResource(it.labelRes) }.joinToString(", ")
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(96.dp).checkerBackground()) { AssetImage(a) }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(stringResource(a.nameRes), style = MaterialTheme.typography.titleSmall)
            Text(a.id, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = Cyan)
            Text("$cats | ${stringResource(a.mouth.labelRes)}", style = MaterialTheme.typography.bodySmall, color = Titanium)
            if (a.review == ReviewState.NeedsReview) Text(stringResource(R.string.lab_needs_review), style = MaterialTheme.typography.bodySmall, color = Orange)
            if (a.language != null) Text(stringResource(R.string.lab_thai_only), style = MaterialTheme.typography.bodySmall, color = Orange)
            Text(a.note, style = MaterialTheme.typography.bodySmall, color = Titanium.copy(alpha = 0.7f))
        }
    }
}
