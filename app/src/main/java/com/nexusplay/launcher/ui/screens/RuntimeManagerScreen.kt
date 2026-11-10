package com.nexusplay.launcher.ui.screens

import androidx.compose.ui.res.stringResource
import com.nexusplay.launcher.core.text
import com.nexusplay.launcher.core.UiText
import com.nexusplay.launcher.R
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexusplay.launcher.core.AppLog
import com.nexusplay.launcher.core.LocalDeviceInfo
import com.nexusplay.launcher.core.LocalResumeTick
import com.nexusplay.launcher.runtime.CompatStatus
import com.nexusplay.launcher.runtime.LaunchResult
import com.nexusplay.launcher.runtime.RuntimeAssessor
import com.nexusplay.launcher.runtime.RuntimeBackend
import com.nexusplay.launcher.runtime.RuntimeDetection
import com.nexusplay.launcher.runtime.RuntimeRegistry
import com.nexusplay.launcher.runtime.RuntimeSelection
import com.nexusplay.launcher.runtime.ShortcutPath
import com.nexusplay.launcher.ui.components.ChamferShape
import com.nexusplay.launcher.ui.components.DeckButton
import com.nexusplay.launcher.ui.components.DeckPanel
import com.nexusplay.launcher.ui.components.DeckTextField
import com.nexusplay.launcher.ui.components.Field
import com.nexusplay.launcher.ui.components.KeyButton
import com.nexusplay.launcher.ui.components.NeonIcons
import com.nexusplay.launcher.ui.components.Readout
import com.nexusplay.launcher.ui.components.SectionTitle
import com.nexusplay.launcher.ui.components.StatusLed
import com.nexusplay.launcher.ui.theme.Cyan
import com.nexusplay.launcher.ui.theme.Lime
import com.nexusplay.launcher.ui.theme.Orange
import com.nexusplay.launcher.ui.theme.TextMuted
import com.nexusplay.launcher.ui.theme.Titanium
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Red = Color(0xFFFF5A4F)

private fun statusColor(s: CompatStatus) = when (s) {
    CompatStatus.Compatible -> Lime
    CompatStatus.Partial -> Orange
    CompatStatus.Unknown -> Cyan
    CompatStatus.Incompatible -> Red
    CompatStatus.RuntimeMissing -> TextMuted
}

private fun statusLabelRes(s: CompatStatus) = when (s) {
    CompatStatus.Compatible -> R.string.rt_status_compatible
    CompatStatus.Partial -> R.string.rt_status_partial
    CompatStatus.Unknown -> R.string.rt_status_unknown
    CompatStatus.Incompatible -> R.string.rt_status_incompatible
    CompatStatus.RuntimeMissing -> R.string.rt_status_missing
}

@Composable
fun RuntimeManagerScreen(contentPadding: PaddingValues, onBack: () -> Unit) {
    val context = LocalContext.current
    val resumeTick = LocalResumeTick.current
    var rescan by remember { mutableIntStateOf(0) }

    // Re-detect on first show, on Rescan, and whenever the app returns to the foreground
    // (for example after installing a runtime).
    val detections: Map<String, RuntimeDetection> = remember(resumeTick, rescan) {
        RuntimeRegistry.backends.associate { b ->
            val d = b.detect(context)
            if (d.installed) {
                AppLog.i("Runtime", UiText(R.string.log_runtime_found, b.displayName, d.versionName ?: UiText(R.string.unknown)), "detected ${b.packageName} ${d.versionName}")
            } else {
                AppLog.i("Runtime", UiText(R.string.log_runtime_missing, b.displayName), "${b.packageName} not installed")
            }
            b.id to d
        }
    }
    val selectedId = RuntimeSelection.selectedId
    val selectedMissing = selectedId != null && detections[selectedId]?.installed != true

    Column(
        Modifier
            .fillMaxSize()
            .padding(top = contentPadding.calculateTopPadding() + 8.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KeyButton(stringResource(R.string.action_back), onBack)
            Spacer(Modifier.width(14.dp))
            Text(stringResource(R.string.rt_title), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            KeyButton(stringResource(R.string.action_rescan), { rescan++ })
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(top = 16.dp, bottom = contentPadding.calculateBottomPadding() + 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Text(
                    stringResource(R.string.rt_intro),
                    style = MaterialTheme.typography.bodyMedium, color = Titanium,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            if (selectedMissing) {
                item {
                    Text(
                        stringResource(R.string.rt_selected_missing),
                        style = MaterialTheme.typography.bodyMedium, color = Orange,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
            RuntimeRegistry.backends.forEachIndexed { index, backend ->
                item(key = backend.id) {
                    RuntimeCard(
                        backend = backend,
                        detection = detections.getValue(backend.id),
                        selected = backend.id == selectedId,
                        shape = if (index % 2 == 0) ChamferShape(20.dp, topLeft = true, bottomRight = true)
                        else ChamferShape(20.dp, topRight = true, bottomLeft = true),
                        onSelect = { RuntimeSelection.select(context, backend.id) }
                    )
                }
            }
            item {
                Text(
                    stringResource(R.string.rt_default_note),
                    style = MaterialTheme.typography.bodySmall, color = TextMuted,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            item { LogPanel() }
        }
    }
}

@Composable
private fun RuntimeCard(
    backend: RuntimeBackend,
    detection: RuntimeDetection,
    selected: Boolean,
    shape: ChamferShape,
    onSelect: () -> Unit
) {
    val context = LocalContext.current
    val device = LocalDeviceInfo.current
    val assessment = remember(detection) {
        RuntimeAssessor.assess(detection, Build.VERSION.SDK_INT, device.is64BitArm, device.primaryAbi, device.hasVulkan)
    }
    val caps = remember(detection) { RuntimeAssessor.capabilities(detection) }
    var expanded by remember { mutableStateOf(false) }
    var path by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf<LaunchResult?>(null) }
    val color = statusColor(assessment.status)

    DeckPanel(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = shape,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(backend.displayName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                StatusLed(color)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(statusLabelRes(assessment.status)), style = MaterialTheme.typography.labelSmall, color = color)
            }
            Text(assessment.summary.text(), style = MaterialTheme.typography.bodyMedium, color = Titanium)
            if (assessment.evidence.isNotEmpty()) {
                Column {
                    Readout(stringResource(R.string.rt_package), backend.packageName)
                    assessment.evidence.forEach {
                        Readout(
                            it.label.text(), it.value.text(),
                            valueColor = when (it.ok) { true -> Lime; false -> Orange; null -> Cyan }
                        )
                    }
                }
            }

            if (detection.installed) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KeyButton(
                        stringResource(if (selected) R.string.rt_selected else R.string.rt_use), onSelect,
                        modifier = Modifier.weight(1f), enabled = !selected, highlight = selected
                    )
                    KeyButton(
                        stringResource(R.string.action_open_app), { feedback = backend.openApp(context) },
                        modifier = Modifier.weight(1f), enabled = caps.canOpenApp
                    )
                }
            } else {
                KeyButton(stringResource(R.string.action_open_project), { openUrl(context, backend.projectUrl) }, Modifier.fillMaxWidth())
            }

            if (caps.canLaunchShortcut) {
                Field(stringResource(R.string.rt_test_field)) {
                    DeckTextField(path, { path = it; feedback = null }, "/storage/emulated/0/.../game.desktop")
                }
                DeckButton(
                    text = stringResource(R.string.rt_launch_shortcut),
                    onClick = {
                        val error = ShortcutPath.validate(path)
                        feedback = if (error != null) LaunchResult.Failed(error)
                        else backend.launchShortcut(context, path)
                    },
                    icon = NeonIcons.Library
                )
            }

            when (val f = feedback) {
                is LaunchResult.HandedOver -> Row(verticalAlignment = Alignment.Top) {
                    StatusLed(Lime, Modifier.padding(top = 6.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(f.detail.text(), style = MaterialTheme.typography.bodySmall, color = Titanium)
                }
                is LaunchResult.Failed -> Row(verticalAlignment = Alignment.Top) {
                    StatusLed(Orange, Modifier.padding(top = 6.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(f.reason.text(), style = MaterialTheme.typography.bodySmall, color = Orange)
                }
                null -> Unit
            }

            KeyButton(
                stringResource(if (expanded) R.string.rt_setup_hide else R.string.rt_setup_show),
                { expanded = !expanded }, Modifier.fillMaxWidth()
            )
            if (expanded) {
                Text(stringResource(R.string.rt_setup_heading), style = MaterialTheme.typography.titleSmall, color = Lime)
                backend.setupSteps.forEachIndexed { i, step ->
                    Text("${i + 1}. ${step.text()}", style = MaterialTheme.typography.bodyMedium)
                }
                Text(stringResource(R.string.rt_limits_heading), style = MaterialTheme.typography.titleSmall, color = Orange)
                backend.knownLimits.forEach {
                    Text(it.text(), style = MaterialTheme.typography.bodyMedium, color = Titanium)
                }
                KeyButton(stringResource(R.string.action_open_project), { openUrl(context, backend.projectUrl) }, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun LogPanel() {
    val entries = AppLog.entries
    val format = remember { SimpleDateFormat("HH:mm:ss", Locale.US) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle(stringResource(R.string.rt_log_title))
        DeckPanel(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = ChamferShape(14.dp, topLeft = true),
            contentPadding = PaddingValues(14.dp)
        ) {
            if (entries.isEmpty()) {
                Text(stringResource(R.string.rt_log_empty), style = MaterialTheme.typography.bodySmall, color = TextMuted)
            } else {
                entries.takeLast(12).reversed().forEach { e ->
                    Text(
                        "${format.format(Date(e.timeMillis))}  ${e.message.text()}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = if (e.level == "INFO") Titanium else Orange,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

private fun openUrl(context: android.content.Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) {
        AppLog.w("Runtime", UiText(R.string.log_project_page_failed), "no activity for ACTION_VIEW: $url")
    }
}
