package com.nexusplay.launcher.ui.screens

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexusplay.launcher.R
import com.nexusplay.launcher.core.AppLanguage
import com.nexusplay.launcher.core.LanguageManager
import com.nexusplay.launcher.core.findActivity
import com.nexusplay.launcher.ui.components.ChamferShape
import com.nexusplay.launcher.ui.components.Corner
import com.nexusplay.launcher.ui.components.DeckButton
import com.nexusplay.launcher.ui.components.DeckPanel
import com.nexusplay.launcher.ui.components.HeaderRule
import com.nexusplay.launcher.ui.components.KeyButton
import com.nexusplay.launcher.ui.components.LogoMark
import com.nexusplay.launcher.ui.components.NeonIcons
import com.nexusplay.launcher.ui.components.StatusLed
import com.nexusplay.launcher.ui.theme.Graphite0
import com.nexusplay.launcher.ui.theme.Graphite3
import com.nexusplay.launcher.ui.theme.Lime
import com.nexusplay.launcher.ui.theme.Orange
import com.nexusplay.launcher.ui.theme.Titanium
import kotlinx.coroutines.delay

/** First launch only: pick a language before entering the app. Shown in both languages at once. */
@Composable
fun FirstLaunchLanguageScreen() {
    val context = LocalContext.current
    val activity = context.findActivity()
    var selected by remember { mutableStateOf(LanguageManager.suggested(context)) }
    var failed by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LogoMark(46.dp)
            Spacer(Modifier.width(14.dp))
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
        }
        Column {
            Text(stringResource(R.string.lang_picker_title), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            HeaderRule()
        }
        LanguageChoices(selected) { selected = it; failed = false }
        ConfirmArea(
            selected = selected,
            labelRes = R.string.lang_confirm,
            enabled = true,
            failed = failed,
            onConfirm = { failed = activity == null || !LanguageManager.apply(activity, selected) }
        )
    }
}

/** Settings > Language: same picker, applied by restarting the Activity cleanly. */
@Composable
fun LanguageSettingsScreen(contentPadding: PaddingValues, onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val current = remember { LanguageManager.saved(context) ?: LanguageManager.suggested(context) }
    var selected by remember { mutableStateOf(current) }
    var failed by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(top = contentPadding.calculateTopPadding() + 8.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = contentPadding.calculateBottomPadding() + 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            KeyButton(stringResource(R.string.action_back), onBack)
            Spacer(Modifier.width(14.dp))
            Text(stringResource(R.string.lang_settings_title), style = MaterialTheme.typography.headlineMedium)
        }
        Text(stringResource(R.string.lang_settings_subtitle), style = MaterialTheme.typography.bodyMedium, color = Titanium)
        LanguageChoices(selected) { selected = it; failed = false }
        ConfirmArea(
            selected = selected,
            labelRes = if (selected == current) R.string.lang_current else R.string.lang_apply,
            enabled = selected != current,
            failed = failed,
            onConfirm = {
                failed = activity == null || !LanguageManager.apply(activity, selected)
            }
        )
    }
}

@Composable
private fun LanguageChoices(selected: AppLanguage, onSelect: (AppLanguage) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        LanguageCard(
            index = 0,
            selected = selected == AppLanguage.Thai,
            shape = ChamferShape(24.dp, topLeft = true, bottomRight = true),
            screws = setOf(Corner.TopRight, Corner.BottomLeft),
            nativeRes = R.string.lang_thai_native,
            labelRes = R.string.lang_thai_label,
            glyphRes = R.string.lang_thai_glyph
        ) { onSelect(AppLanguage.Thai) }
        LanguageCard(
            index = 1,
            selected = selected == AppLanguage.English,
            shape = ChamferShape(24.dp, topRight = true, bottomLeft = true),
            screws = setOf(Corner.TopLeft, Corner.BottomRight),
            nativeRes = R.string.lang_english_native,
            labelRes = R.string.lang_english_label,
            glyphRes = R.string.lang_english_glyph
        ) { onSelect(AppLanguage.English) }
    }
}

@Composable
private fun LanguageCard(
    index: Int,
    selected: Boolean,
    shape: ChamferShape,
    screws: Set<Corner>,
    @StringRes nativeRes: Int,
    @StringRes labelRes: Int,
    @StringRes glyphRes: Int,
    onClick: () -> Unit
) {
    val sel by animateFloatAsState(if (selected) 1f else 0f, tween(220), label = "languageSelected")
    val enter = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(index * 110L)
        enter.animateTo(1f, tween(380, easing = FastOutSlowInEasing))
    }

    Box(
        Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = enter.value
                translationY = (1f - enter.value) * 40f
            }
            .semantics { this.selected = selected }
            .drawWithContent {
                drawContent()
                val h = size.height * 0.6f * sel
                drawRect(Lime, Offset(0f, (size.height - h) / 2f), Size(4.dp.toPx(), h))
            }
    ) {
        DeckPanel(
            modifier = Modifier.fillMaxWidth(),
            shape = shape,
            screws = screws,
            onClick = onClick,
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(64.dp)
                        .clip(ChamferShape(12.dp, topLeft = true))
                        .background(Graphite0.copy(alpha = 0.8f))
                        .border(1.dp, lerp(Graphite3, Lime, sel), ChamferShape(12.dp, topLeft = true)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(glyphRes),
                        style = MaterialTheme.typography.headlineMedium,
                        color = lerp(Titanium, Lime, sel)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(nativeRes),
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp, lineHeight = 46.sp)
                    )
                    Text(stringResource(labelRes), style = MaterialTheme.typography.bodyMedium, color = Titanium)
                }
                Spacer(Modifier.width(12.dp))
                StatusLed(Lime, lit = selected)
            }
        }
    }
}

/** Hint, error and confirm button, written in the language currently highlighted (a live preview). */
@Composable
private fun ConfirmArea(
    selected: AppLanguage,
    @StringRes labelRes: Int,
    enabled: Boolean,
    failed: Boolean,
    onConfirm: () -> Unit
) {
    val base = LocalContext.current
    val preview = remember(selected, base) { LanguageManager.localized(base, selected) }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(preview.getString(R.string.lang_hint), style = MaterialTheme.typography.bodyMedium, color = Titanium)
        if (failed) {
            Row(verticalAlignment = Alignment.Top) {
                StatusLed(Orange, Modifier.padding(top = 6.dp))
                Spacer(Modifier.width(10.dp))
                Text(preview.getString(R.string.lang_save_failed), style = MaterialTheme.typography.bodyMedium, color = Orange)
            }
        }
        DeckButton(
            text = preview.getString(labelRes),
            onClick = onConfirm,
            icon = NeonIcons.Globe,
            enabled = enabled
        )
    }
}
