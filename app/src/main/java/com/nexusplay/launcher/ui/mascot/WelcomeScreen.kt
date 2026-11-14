package com.nexusplay.launcher.ui.mascot

import com.nexusplay.launcher.mascot.MascotEvent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nexusplay.launcher.R
import com.nexusplay.launcher.mascot.LocalMascot
import com.nexusplay.launcher.ui.components.ChamferShape
import com.nexusplay.launcher.ui.components.Corner
import com.nexusplay.launcher.ui.components.DeckBackground
import com.nexusplay.launcher.ui.components.DeckButton
import com.nexusplay.launcher.ui.components.DeckPanel
import com.nexusplay.launcher.ui.components.KeyButton
import com.nexusplay.launcher.ui.components.NeonIcons
import com.nexusplay.launcher.ui.theme.TextMuted
import com.nexusplay.launcher.ui.theme.Titanium

/** Shown once after the first boot (and again on request from Settings). Opaque, so it covers the app. */
@Composable
fun WelcomeOverlay() {
    val mascot = LocalMascot.current
    val enter = remember { Animatable(if (mascot.animations) 0f else 1f) }
    LaunchedEffect(Unit) { enter.animateTo(1f, tween(520, easing = FastOutSlowInEasing)) }

    DeckBackground {
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EventImage(
                MascotEvent.Welcome, 240.dp,
                Modifier.graphicsLayer {
                    alpha = enter.value
                    val s = 0.88f + 0.12f * enter.value
                    scaleX = s; scaleY = s
                }
            )
            DeckPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = enter.value },
                shape = ChamferShape(22.dp, topLeft = true, bottomRight = true),
                screws = setOf(Corner.TopRight, Corner.BottomLeft),
                contentPadding = PaddingValues(18.dp)
            ) {
                Text(stringResource(R.string.welcome_title), style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.welcome_body), style = MaterialTheme.typography.bodyMedium, color = Titanium)
            }
            DeckButton(
                text = stringResource(R.string.welcome_start),
                onClick = { mascot.completeWelcome(startTour = true) },
                icon = NeonIcons.Mascot
            )
            KeyButton(stringResource(R.string.welcome_skip), { mascot.completeWelcome(startTour = false) }, Modifier.fillMaxWidth())
            Text(
                stringResource(R.string.welcome_footer),
                style = MaterialTheme.typography.bodySmall, color = TextMuted, textAlign = TextAlign.Center
            )
        }
    }
}
