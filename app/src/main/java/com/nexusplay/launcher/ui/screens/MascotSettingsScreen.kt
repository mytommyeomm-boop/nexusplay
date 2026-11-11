package com.nexusplay.launcher.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nexusplay.launcher.R
import com.nexusplay.launcher.core.UiText
import com.nexusplay.launcher.mascot.LocalMascot
import com.nexusplay.launcher.mascot.MascotNotice
import com.nexusplay.launcher.mascot.MascotPose
import com.nexusplay.launcher.mascot.NoticeKind
import com.nexusplay.launcher.mascot.Tutorials
import com.nexusplay.launcher.ui.components.ChamferShape
import com.nexusplay.launcher.ui.components.DeckPanel
import com.nexusplay.launcher.ui.components.DeckSegmented
import com.nexusplay.launcher.ui.components.KeyButton
import com.nexusplay.launcher.ui.components.StatusLed
import com.nexusplay.launcher.ui.components.ToggleRow
import com.nexusplay.launcher.ui.mascot.MascotImage
import com.nexusplay.launcher.ui.theme.Lime
import com.nexusplay.launcher.ui.theme.TextMuted
import com.nexusplay.launcher.ui.theme.Titanium

@Composable
fun MascotSettingsScreen(contentPadding: PaddingValues, onBack: () -> Unit) {
    val mascot = LocalMascot.current
    val poses = remember { MascotPose.entries }
    var previewIndex by remember { mutableIntStateOf(0) }
    val pose = poses[previewIndex]

    Column(
        Modifier
            .fillMaxSize()
            .padding(top = contentPadding.calculateTopPadding() + 8.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = contentPadding.calculateBottomPadding() + 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            KeyButton(stringResource(R.string.action_back), onBack)
            Spacer(Modifier.width(14.dp))
            Text(stringResource(R.string.mascot_title), style = MaterialTheme.typography.headlineMedium)
        }
        Text(stringResource(R.string.mascot_subtitle), style = MaterialTheme.typography.bodyMedium, color = Titanium)

        // Tap the preview to step through every pose she has.
        DeckPanel(
            modifier = Modifier.fillMaxWidth(),
            shape = ChamferShape(22.dp, topLeft = true, bottomRight = true),
            onClick = if (mascot.enabled) { { previewIndex = (previewIndex + 1) % poses.size } } else null,
            contentPadding = PaddingValues(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (mascot.enabled) {
                    Box(Modifier.size(150.dp), contentAlignment = Alignment.BottomCenter) { MascotImage(pose, 140.dp) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(pose.nameRes), style = MaterialTheme.typography.titleMedium, color = Lime)
                        Text(stringResource(R.string.mascot_preview_hint), style = MaterialTheme.typography.bodySmall, color = Titanium)
                    }
                } else {
                    Text(stringResource(R.string.mascot_off_hint), style = MaterialTheme.typography.bodyMedium, color = Titanium)
                }
            }
        }

        Panel(ChamferShape(16.dp, topRight = true)) {
            ToggleRow(stringResource(R.string.mascot_enabled), mascot.enabled, { mascot.setEnabled(it) }, caption = stringResource(R.string.mascot_enabled_cap))
            ToggleRow(stringResource(R.string.mascot_animations), mascot.animations, { mascot.setAnimations(it) }, caption = stringResource(R.string.mascot_animations_cap))
            ToggleRow(stringResource(R.string.mascot_tips), mascot.tutorialSuggestions, { mascot.setTutorialSuggestions(it) }, caption = stringResource(R.string.mascot_tips_cap))
            ToggleRow(stringResource(R.string.mascot_notices), mascot.notifications, { mascot.setNotifications(it) }, caption = stringResource(R.string.mascot_notices_cap))
            Text(stringResource(R.string.mascot_size), style = MaterialTheme.typography.titleSmall, color = Titanium)
            DeckSegmented(
                listOf(
                    stringResource(R.string.mascot_size_small),
                    stringResource(R.string.mascot_size_medium),
                    stringResource(R.string.mascot_size_large)
                ),
                mascot.sizeLevel
            ) { mascot.setSizeLevel(it) }
        }

        Text(stringResource(R.string.mascot_tutorials), style = MaterialTheme.typography.titleMedium, color = Titanium)
        Panel(ChamferShape(16.dp, bottomLeft = true)) {
            Tutorials.all.forEach { t ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(t.nameRes), style = MaterialTheme.typography.titleSmall)
                        if (mascot.isCompleted(t.id)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StatusLed(Lime)
                                Spacer(Modifier.width(6.dp))
                                Text(stringResource(R.string.mascot_seen), style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            }
                        }
                    }
                    KeyButton(stringResource(R.string.mascot_play), { mascot.requestTutorial(t) })
                }
            }
            KeyButton(
                stringResource(R.string.mascot_reset_tutorials),
                {
                    mascot.resetTutorials()
                    mascot.notify(
                        MascotNotice(
                            "tutorials_reset", NoticeKind.Info,
                            UiText(R.string.notice_tutorials_reset_title), UiText(R.string.notice_tutorials_reset_message)
                        ),
                        cooldownMs = 0L, force = true
                    )
                },
                Modifier.fillMaxWidth()
            )
            KeyButton(stringResource(R.string.mascot_replay_welcome), { mascot.showWelcomeAgain() }, Modifier.fillMaxWidth())
        }

        KeyButton(
            stringResource(R.string.mascot_test_notice),
            {
                mascot.notify(
                    MascotNotice(
                        "preview", NoticeKind.Info,
                        UiText(R.string.notice_preview_title), UiText(R.string.notice_preview_message)
                    ),
                    cooldownMs = 0L, force = true
                )
            },
            Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Panel(shape: ChamferShape, content: @Composable ColumnScope.() -> Unit) {
    DeckPanel(Modifier.fillMaxWidth(), shape = shape, contentPadding = PaddingValues(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}
