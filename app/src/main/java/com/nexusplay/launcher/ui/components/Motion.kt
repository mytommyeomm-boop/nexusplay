package com.nexusplay.launcher.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/** Fade-and-rise entrance, staggered by [index]. Values are read in the draw phase only. */
@Composable
fun Modifier.staggeredEntrance(index: Int): Modifier {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 450,
                delayMillis = index.coerceAtMost(6) * 70,
                easing = FastOutSlowInEasing
            )
        )
    }
    return this.graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * 36f
    }
}
