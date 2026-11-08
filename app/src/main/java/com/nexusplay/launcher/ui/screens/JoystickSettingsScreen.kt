package com.nexusplay.launcher.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.window.Dialog
import com.nexusplay.launcher.input.JoystickConfig
import com.nexusplay.launcher.input.JoystickLimits
import com.nexusplay.launcher.input.JoystickMath
import com.nexusplay.launcher.input.JoystickOutput
import com.nexusplay.launcher.input.JoystickRepository
import com.nexusplay.launcher.input.ResponseMode
import com.nexusplay.launcher.ui.components.ChamferShape
import com.nexusplay.launcher.ui.components.HwSegmented
import com.nexusplay.launcher.ui.components.HwSlider
import com.nexusplay.launcher.ui.components.HwToggleRow
import com.nexusplay.launcher.ui.components.JoystickPad
import com.nexusplay.launcher.ui.components.LampTag
import com.nexusplay.launcher.ui.components.SectionLabel
import com.nexusplay.launcher.ui.components.SmallKey
import com.nexusplay.launcher.ui.components.HwPanel
import com.nexusplay.launcher.ui.theme.AccentCyan
import com.nexusplay.launcher.ui.theme.AccentLime
import com.nexusplay.launcher.ui.theme.AccentOrange
import com.nexusplay.launcher.ui.theme.Graphite0
import com.nexusplay.launcher.ui.theme.Graphite1
import com.nexusplay.launcher.ui.theme.Graphite2
import com.nexusplay.launcher.ui.theme.Hairline
import com.nexusplay.launcher.ui.theme.TextMuted
import com.nexusplay.launcher.ui.theme.TextPrimary
import com.nexusplay.launcher.ui.theme.TextSecondary
import com.nexusplay.launcher.ui.theme.Titanium
import java.util.Locale
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

private class OutputHolder { var value by mutableStateOf(JoystickOutput()) }

private fun pct(v: Float) = "${(v * 100).roundToInt()}%"
private fun mult(v: Float) = String.format(Locale.US, "%.2fx", v)

private val modeDescriptions = mapOf(
    ResponseMode.Linear to "Output follows your finger one to one.",
    ResponseMode.Curve to "Output follows the response curve below.",
    ResponseMode.Smooth to "Curve plus filtering, so the stick eases toward your finger.",
    ResponseMode.Precision to "Lower gain and a flat start for fine aiming.",
    ResponseMode.Fast to "Higher gain and a steep start, full output sooner."
)

@Composable
fun JoystickSettingsScreen(contentPadding: PaddingValues, onBack: () -> Unit) {
    val repo = JoystickRepository.get(LocalContext.current)
    val settings = repo.settings
    val cfg = settings.working
    val holder = remember { OutputHolder() }
    val haptics = LocalHapticFeedback.current
    val set: ((JoystickConfig) -> JoystickConfig) -> Unit = { f -> repo.update { it.edit(f(it.working)) } }

    var dialog by remember { mutableStateOf<DialogKind?>(null) }
    var confirmDelete by remember(settings.activeId) { mutableStateOf(false) }
    LaunchedEffect(confirmDelete) { if (confirmDelete) { delay(3000); confirmDelete = false } }

    Column(
        Modifier
            .fillMaxSize()
            .padding(top = contentPadding.calculateTopPadding() + 8.dp, bottom = contentPadding.calculateBottomPadding())
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmallKey("Back", onBack, accent = Titanium)
            Spacer(Modifier.width(14.dp))
            Text("JOYSTICK", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
            Spacer(Modifier.weight(1f))
            if (settings.modified) LampTag("Custom", AccentOrange)
        }
        Spacer(Modifier.height(10.dp))
        TestArea(cfg, holder)
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { SectionLabel("Presets", Modifier.padding(horizontal = 0.dp)) }
            item {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    settings.all.forEach { p ->
                        PresetChip(
                            name = p.name,
                            selected = p.id == settings.activeId,
                            modified = p.id == settings.activeId && settings.modified,
                            onClick = { repo.update { it.select(p.id) } }
                        )
                    }
                }
            }
            item { PresetActions(settings.activePreset.builtIn, settings.modified, confirmDelete,
                onSave = { repo.update { it.overwriteActive() } },
                onSaveAs = { dialog = DialogKind.SaveAs },
                onRename = { dialog = DialogKind.Rename },
                onDuplicate = { repo.update { it.duplicate(it.activeId) } },
                onRevert = { repo.update { it.revert() } },
                onDefaults = { repo.update { it.restoreDefaults() } },
                onDelete = {
                    if (confirmDelete) { repo.update { it.delete(it.activeId) }; confirmDelete = false }
                    else confirmDelete = true
                }
            ) }

            item { SectionLabel("Basic") }
            item { HwToggleRow("Joystick enabled", cfg.enabled, { v -> set { it.copy(enabled = v) } },
                caption = "Off: the stick ignores touches and outputs zero.") }
            item { HwSlider("Size", "${cfg.sizeDp.roundToInt()} dp", cfg.sizeDp, JoystickLimits.size,
                { v -> set { it.copy(sizeDp = v) } }, step = 1f) }
            item { HwSlider("Opacity", pct(cfg.opacity), cfg.opacity, JoystickLimits.opacity,
                { v -> set { it.copy(opacity = v) } }) }
            item { HwSlider("Position X", pct(cfg.posX), cfg.posX, JoystickLimits.position,
                { v -> set { it.copy(posX = v) } }, enabled = !cfg.floating,
                note = if (cfg.floating) "Floating mode: the stick appears where you touch." else null) }
            item { HwSlider("Position Y", pct(cfg.posY), cfg.posY, JoystickLimits.position,
                { v -> set { it.copy(posY = v) } }, enabled = !cfg.floating) }
            item { HwSlider("Sensitivity", mult(cfg.sensitivity), cfg.sensitivity, JoystickLimits.sensitivity,
                { v -> set { it.copy(sensitivity = v) } }) }
            item { HwSlider("Horizontal sensitivity", mult(cfg.sensitivityX), cfg.sensitivityX, JoystickLimits.sensitivity,
                { v -> set { it.copy(sensitivityX = v) } }) }
            item { HwSlider("Vertical sensitivity", mult(cfg.sensitivityY), cfg.sensitivityY, JoystickLimits.sensitivity,
                { v -> set { it.copy(sensitivityY = v) } }) }
            item { HwSlider("Movement range", mult(cfg.range), cfg.range, JoystickLimits.range,
                { v -> set { it.copy(range = v) } },
                note = "Finger travel for full output, relative to the base. Lower = shorter reach.") }
            item { HwSlider("Inner deadzone", pct(cfg.innerDeadzone), cfg.innerDeadzone, JoystickLimits.innerDeadzone,
                { v -> set { it.copy(innerDeadzone = v) } }) }
            item { HwToggleRow("Return to center", cfg.returnToCenter, { v -> set { it.copy(returnToCenter = v) } },
                caption = "Off: the stick stays where you release it. Touch the center to recenter.") }

            item { SectionLabel("Advanced") }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Response mode", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                    HwSegmented(
                        ResponseMode.entries.map { it.label },
                        cfg.mode.ordinal,
                        { i -> set { it.copy(mode = ResponseMode.entries[i]) } }
                    )
                    Text(modeDescriptions.getValue(cfg.mode), style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
            }
            item { HwSlider("Response curve", String.format(Locale.US, "%.2f", cfg.curve), cfg.curve, JoystickLimits.curve,
                { v -> set { it.copy(curve = v) } }, enabled = cfg.mode.usesCurve,
                note = if (cfg.mode.usesCurve) "Below 1.0 reacts more near center, above 1.0 less." else "Only used by Curve and Smooth modes.") }
            item { HwSlider("Outer deadzone", pct(cfg.outerDeadzone), cfg.outerDeadzone, JoystickLimits.outerDeadzone,
                { v -> set { it.copy(outerDeadzone = v) } },
                note = "Full output is reached this far before the edge.") }
            item { HwSlider("Acceleration", pct(cfg.acceleration), cfg.acceleration, JoystickLimits.acceleration,
                { v -> set { it.copy(acceleration = v) } },
                note = "Output eases in after you touch, up to 0.4 s at 100%.") }
            item { HwSlider("Return speed",
                if (cfg.returnSpeed >= 0.995f) "Instant" else "${((1f - cfg.returnSpeed) * 500f).roundToInt()} ms",
                cfg.returnSpeed, JoystickLimits.returnSpeed, { v -> set { it.copy(returnSpeed = v) } },
                enabled = cfg.returnToCenter,
                note = if (cfg.returnToCenter) null else "Needs Return to center.") }
            item { HwToggleRow("Floating stick", cfg.floating, { v -> set { it.copy(floating = v) } },
                caption = "On: the stick appears under your thumb (left half of the screen). Off: fixed position.") }
            item { HwToggleRow("Invert horizontal", cfg.invertX, { v -> set { it.copy(invertX = v) } }) }
            item { HwToggleRow("Invert vertical", cfg.invertY, { v -> set { it.copy(invertY = v) } }) }
            item { HwToggleRow("Lock horizontal axis", cfg.lockX,
                { v -> set { it.copy(lockX = v, lockY = if (v) false else it.lockY) } },
                caption = "Stick moves vertically only.") }
            item { HwToggleRow("Lock vertical axis", cfg.lockY,
                { v -> set { it.copy(lockY = v, lockX = if (v) false else it.lockX) } },
                caption = "Stick moves horizontally only.") }
            item { HwToggleRow("Vibration feedback", cfg.vibration,
                { v ->
                    set { it.copy(vibration = v) }
                    if (v) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                caption = "A light tick when you touch and at full deflection. Follows the phone's touch-vibration setting.") }

            item { SectionLabel("Status") }
            item { StatusPanel() }
        }
    }

    when (dialog) {
        DialogKind.SaveAs -> NameDialog("Save as new preset", "", "Save",
            onConfirm = { name -> repo.update { it.saveAsNew(name) }; dialog = null }, onDismiss = { dialog = null })
        DialogKind.Rename -> NameDialog("Rename preset", settings.activePreset.name, "Rename",
            onConfirm = { name -> repo.update { it.rename(it.activeId, name) }; dialog = null }, onDismiss = { dialog = null })
        null -> Unit
    }
}

private enum class DialogKind { SaveAs, Rename }

@Composable
private fun TestArea(cfg: JoystickConfig, holder: OutputHolder) {
    val shape = ChamferShape(topRight = 18.dp, bottomLeft = 18.dp)
    Box(
        Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .height(232.dp)
            .clip(shape)
            .background(Graphite0)
            .border(1.dp, Hairline, shape)
    ) {
        JoystickPad(cfg, showGuides = true, onOutput = { holder.value = it })
        Readout(holder, Modifier.align(Alignment.TopStart).padding(12.dp))
        CurveGraph(cfg, holder, Modifier.align(Alignment.TopEnd).padding(12.dp).size(width = 96.dp, height = 72.dp))
        Text(
            "TEST AREA - NOTHING IS SENT TO A GAME",
            style = MaterialTheme.typography.labelSmall, color = TextMuted,
            modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)
        )
    }
}

@Composable
private fun Readout(holder: OutputHolder, modifier: Modifier) {
    val o = holder.value
    fun f(v: Float) = String.format(Locale.US, "%+.2f", v)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text("X ${f(o.x)}", style = MaterialTheme.typography.labelLarge, color = AccentLime)
        Text("Y ${f(o.y)}", style = MaterialTheme.typography.labelLarge, color = AccentLime)
        Text("M ${String.format(Locale.US, "%.2f", o.magnitude)}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Text(o.direction, style = MaterialTheme.typography.labelSmall, color = AccentCyan)
    }
}

/** Plots the real response (same math as the stick) with a dot at the current finger deflection. */
@Composable
private fun CurveGraph(cfg: JoystickConfig, holder: OutputHolder, modifier: Modifier) {
    Canvas(modifier) {
        val plot = cfg.copy(lockX = false, invertX = false)
        drawRect(Graphite1.copy(alpha = 0.7f))
        drawRect(Hairline, style = Stroke(1f))
        drawLine(Hairline, Offset(0f, size.height), Offset(size.width, 0f), 1f) // 1:1 reference
        var prev: Offset? = null
        for (i in 0..40) {
            val d = i / 40f
            val y = JoystickMath.shape(d, 0f, plot).x.coerceIn(0f, 1f)
            val p = Offset(d * size.width, size.height - y * size.height)
            prev?.let { drawLine(AccentLime, it, p, 2f) }
            prev = p
        }
        val o = holder.value
        if (o.active) {
            val d = hypot(o.thumbX, o.thumbY).coerceIn(0f, 1f)
            drawCircle(AccentOrange, 4.dp.toPx(), Offset(d * size.width, size.height - o.magnitude * size.height))
        }
    }
}

@Composable
private fun PresetChip(name: String, selected: Boolean, modified: Boolean, onClick: () -> Unit) {
    val shape = ChamferShape(topRight = 8.dp)
    Box(
        Modifier
            .height(44.dp)
            .clip(shape)
            .background(if (selected) AccentLime else Graphite2)
            .border(1.dp, if (selected) Color.Transparent else Hairline, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            name.uppercase() + if (modified) " *" else "",
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) Graphite0 else Titanium, maxLines = 1
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PresetActions(
    builtIn: Boolean, modified: Boolean, confirmDelete: Boolean,
    onSave: () -> Unit, onSaveAs: () -> Unit, onRename: () -> Unit, onDuplicate: () -> Unit,
    onRevert: () -> Unit, onDefaults: () -> Unit, onDelete: () -> Unit
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SmallKey("Save", onSave, enabled = !builtIn && modified)
        SmallKey("Save as new", onSaveAs)
        SmallKey("Rename", onRename, enabled = !builtIn)
        SmallKey("Duplicate", onDuplicate)
        SmallKey("Revert", onRevert, enabled = modified, accent = AccentCyan)
        SmallKey("Default", onDefaults, accent = AccentCyan)
        SmallKey(if (confirmDelete) "Tap again to delete" else "Delete", onDelete, enabled = !builtIn, accent = AccentOrange)
    }
}

@Composable
private fun StatusPanel() {
    HwPanel(Modifier.fillMaxWidth(), accent = AccentCyan) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LampTag("Stick math, presets, saving: live", AccentLime)
            LampTag("Test area: live", AccentLime)
            LampTag("Sending input to a game: not connected", AccentOrange)
            Text(
                "Settings are saved on this phone as you change them. Games only receive stick values once a runtime with an input bridge is connected; an overlay cannot inject input into other apps by itself.",
                style = MaterialTheme.typography.bodySmall, color = TextSecondary
            )
        }
    }
}

@Composable
private fun NameDialog(title: String, initial: String, confirm: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf(initial) }
    val shape = ChamferShape(topRight = 18.dp, bottomLeft = 18.dp)
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier.clip(shape).background(Graphite1).border(1.dp, Titanium.copy(alpha = 0.4f), shape).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(title.uppercase(), style = MaterialTheme.typography.labelLarge, color = AccentLime)
            BasicTextField(
                value = text,
                onValueChange = { if (it.length <= 24) text = it },
                singleLine = true,
                textStyle = MaterialTheme.typography.titleMedium.copy(color = TextPrimary),
                cursorBrush = SolidColor(AccentLime),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth().background(Graphite0).border(1.dp, Hairline).padding(14.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallKey("Cancel", onDismiss, accent = Titanium)
                SmallKey(confirm, { onConfirm(text) }, enabled = text.isNotBlank())
            }
        }
    }
}
