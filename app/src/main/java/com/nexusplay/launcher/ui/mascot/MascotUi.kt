package com.nexusplay.launcher.ui.mascot

import com.nexusplay.launcher.mascot.MascotEvent
import com.nexusplay.launcher.mascot.MascotAsset
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nexusplay.launcher.R
import com.nexusplay.launcher.core.text
import com.nexusplay.launcher.mascot.LocalMascot
import com.nexusplay.launcher.mascot.MascotNotice
import com.nexusplay.launcher.mascot.NoticeKind
import com.nexusplay.launcher.mascot.TutorialStep
import com.nexusplay.launcher.ui.components.ChamferShape
import com.nexusplay.launcher.ui.components.DeckPanel
import com.nexusplay.launcher.ui.components.KeyButton
import com.nexusplay.launcher.ui.components.NeonIcons
import com.nexusplay.launcher.ui.components.StatusLed
import com.nexusplay.launcher.ui.theme.Cyan
import com.nexusplay.launcher.ui.theme.Lime
import com.nexusplay.launcher.ui.theme.Orange
import com.nexusplay.launcher.ui.theme.Titanium
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min

private val ErrorRed = Color(0xFFFF5A4F)

/** Marks a piece of UI so a tutorial step can point at it. The entry disappears with the composable. */
@Composable
fun Modifier.tutorialAnchor(key: String): Modifier {
    val anchors = LocalMascot.current.anchors
    DisposableEffect(key) { onDispose { anchors.remove(key) } }
    return this.onGloballyPositioned { anchors[key] = it.boundsInRoot() }
}

/**
 * The mascot art, never stretched (ContentScale.Fit). Size follows the user's size setting; the
 * gentle float runs only when animations are on. Draws nothing when the mascot is switched off.
 */
@Composable
fun MascotImage(asset: MascotAsset, size: Dp, modifier: Modifier = Modifier, floating: Boolean = true) {
    val mascot = LocalMascot.current
    if (!mascot.enabled) return
    val box = size * mascot.sizeFactor
    val motion = if (floating && mascot.animations) Modifier.floatingMotion() else Modifier
    Box(modifier.size(box).then(motion)) {
        if (mascot.animations) {
            Crossfade(targetState = asset, animationSpec = tween(180), label = "mascotPose") { a -> AssetImage(a) }
        } else {
            AssetImage(asset)
        }
    }
}

/** The raw picture, never stretched. painterResource caches decoded bitmaps, so reuse costs nothing. */
@Composable
fun AssetImage(asset: MascotAsset, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(asset.res),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        alignment = Alignment.BottomCenter,
        modifier = modifier.fillMaxSize()
    )
}

/** Shows the art mapped to [event] (see MascotMapping and the developer lab). */
@Composable
fun EventImage(event: MascotEvent, size: Dp, modifier: Modifier = Modifier, floating: Boolean = true) {
    val mascot = LocalMascot.current
    MascotImage(mascot.assetFor(event), size, modifier, floating)
}

@Composable
private fun Modifier.floatingMotion(): Modifier {
    val transition = rememberInfiniteTransition(label = "mascotFloat")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "floatPhase"
    )
    return this.graphicsLayer {
        translationY = -phase * 6.dp.toPx()
        val s = 1f + 0.018f * phase
        scaleX = s
        scaleY = s
        transformOrigin = TransformOrigin(0.5f, 1f)
    }
}

// ------------------------------------------------------------------ notifications

/** Friendly banner above the navigation bar. One at a time, auto-dismissed, always closable. */
@Composable
fun BoxScope.MascotNoticeHost() {
    val mascot = LocalMascot.current
    val current = mascot.notice
    var last by remember { mutableStateOf<MascotNotice?>(null) }
    LaunchedEffect(current) {
        if (current != null) {
            last = current
            delay((current.kind.seconds + if (current.action != null) 4 else 0) * 1000L)
            mascot.dismissNotice(current)
        }
    }
    val animate = mascot.animations
    AnimatedVisibility(
        visible = current != null,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(bottom = 80.dp),
        enter = if (animate) slideInVertically { it / 2 } + fadeIn(tween(200)) else EnterTransition.None,
        exit = if (animate) fadeOut(tween(160)) else ExitTransition.None
    ) {
        (current ?: last)?.let { n -> NoticeCard(n) { mascot.dismissNotice(n) } }
    }
}

@Composable
private fun NoticeCard(n: MascotNotice, onClose: () -> Unit) {
    val mascot = LocalMascot.current
    val accent = when (n.kind) {
        NoticeKind.Error -> ErrorRed
        NoticeKind.Warning, NoticeKind.MissingRuntime, NoticeKind.LaunchIssue -> Orange
        NoticeKind.Success, NoticeKind.GamepadSaved -> Lime
        else -> Cyan
    }
    val closeLabel = stringResource(R.string.action_close)
    val interaction = remember { MutableInteractionSource() }
    DeckPanel(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = ChamferShape(16.dp, topLeft = true, bottomRight = true),
        contentPadding = PaddingValues(start = 8.dp, top = 8.dp, end = 2.dp, bottom = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (mascot.enabled) {
                EventImage(n.kind.event, 76.dp, floating = false)
                Spacer(Modifier.width(8.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusLed(accent)
                    Spacer(Modifier.width(8.dp))
                    Text(n.title.text(), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                }
                Text(n.message.text(), style = MaterialTheme.typography.bodySmall, color = Titanium)
                n.action?.let { a ->
                    Spacer(Modifier.height(4.dp))
                    KeyButton(a.label.text(), { a.onClick(); onClose() }, highlight = true)
                }
            }
            Box(
                Modifier
                    .size(48.dp)
                    .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClickLabel = closeLabel, onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(NeonIcons.Close, contentDescription = closeLabel, tint = Titanium, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// ------------------------------------------------------------------ tutorials

/**
 * Modal tour: dims the screen, cuts a lit window around the target and places the explanation card
 * on whichever side has more room, so the highlighted element is never covered. System Back skips.
 */
@Composable
fun TutorialOverlay() {
    val mascot = LocalMascot.current
    val tutorial = mascot.active ?: return
    BackHandler { mascot.skip() }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val windowH = constraints.maxHeight.toFloat()
        LaunchedEffect(tutorial.id, mascot.resolving) {
            if (mascot.resolving) {
                delay(350) // let the screen finish its transition and report its anchors
                mascot.resolveSteps(windowH)
            }
        }
        val step = mascot.steps.getOrNull(mascot.stepIndex)
        if (step != null) TutorialStepView(step, windowH)
    }
}

@Composable
private fun rememberPulse(enabled: Boolean): State<Float> =
    if (enabled) {
        rememberInfiniteTransition(label = "tutorialPulse").animateFloat(
            initialValue = 0.45f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "pulse"
        )
    } else remember { mutableStateOf(1f) }

@Composable
private fun BoxScope.TutorialStepView(step: TutorialStep, windowH: Float) {
    val mascot = LocalMascot.current
    val rect: Rect? = step.anchor?.let { mascot.anchors[it] }
    val pulse by rememberPulse(mascot.animations)

    // Scrim in four pieces around the hole (no offscreen layer needed) and swallow every touch.
    Canvas(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) awaitPointerEvent().changes.forEach { it.consume() }
                }
            }
    ) {
        val scrim = Color.Black.copy(alpha = 0.72f)
        if (rect == null) {
            drawRect(scrim)
        } else {
            val pad = 8.dp.toPx()
            val l = max(rect.left - pad, 0f); val t = max(rect.top - pad, 0f)
            val r = min(rect.right + pad, size.width); val b = min(rect.bottom + pad, size.height)
            drawRect(scrim, Offset.Zero, Size(size.width, t))
            drawRect(scrim, Offset(0f, b), Size(size.width, size.height - b))
            drawRect(scrim, Offset(0f, t), Size(l, b - t))
            drawRect(scrim, Offset(r, t), Size(size.width - r, b - t))
            val outline = ChamferShape(12.dp, topLeft = true, bottomRight = true).path(Size(r - l, b - t), this)
            translate(l, t) { drawPath(outline, Lime.copy(alpha = pulse), style = Stroke(2.5.dp.toPx())) }
        }
    }

    val atTop = rect != null && (windowH - rect.bottom) < rect.top
    Box(
        Modifier
            .align(if (atTop) Alignment.TopCenter else Alignment.BottomCenter)
            .then(if (atTop) Modifier.statusBarsPadding() else Modifier.navigationBarsPadding())
            .padding(12.dp)
    ) { StepCard(step) }
}

@Composable
private fun StepCard(step: TutorialStep) {
    val mascot = LocalMascot.current
    val index = mascot.stepIndex
    val total = mascot.steps.size
    val enter = remember(index) { Animatable(if (mascot.animations) 0f else 1f) }
    LaunchedEffect(index) { enter.animateTo(1f, tween(240)) }

    DeckPanel(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = enter.value; translationY = (1f - enter.value) * 24f },
        shape = ChamferShape(20.dp, topLeft = true, bottomRight = true),
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            if (mascot.enabled) {
                EventImage(step.event, 92.dp)
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(step.title), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(step.body), style = MaterialTheme.typography.bodyMedium, color = Titanium)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                stringResource(R.string.tut_step, index + 1, total),
                style = MaterialTheme.typography.labelSmall, color = Cyan, modifier = Modifier.weight(1f)
            )
            KeyButton(stringResource(R.string.tut_skip), { mascot.skip() })
            if (index > 0) KeyButton(stringResource(R.string.action_back), { mascot.back() })
            KeyButton(
                stringResource(if (index == total - 1) R.string.tut_done else R.string.tut_next),
                { mascot.next() }, highlight = true
            )
        }
    }
}
