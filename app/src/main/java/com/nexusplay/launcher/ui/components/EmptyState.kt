package com.nexusplay.launcher.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nexusplay.launcher.ui.theme.NeonCyan
import com.nexusplay.launcher.ui.theme.NeonGradient
import com.nexusplay.launcher.ui.theme.Obsidian
import com.nexusplay.launcher.ui.theme.TextSecondary

/** Glowing icon disc with a slow breathing pulse. */
@Composable
fun NeonOrb(icon: ImageVector, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "orb")
    val pulse by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(tween(1900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    Box(
        modifier = modifier
            .size(88.dp)
            .graphicsLayer { scaleX = pulse; scaleY = pulse }
            .background(
                Brush.radialGradient(listOf(NeonCyan.copy(alpha = 0.30f), Color.Transparent)),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(Obsidian.copy(alpha = 0.65f))
                .border(1.dp, NeonGradient, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(28.dp))
        }
    }
}

/** Empty screen = an invitation to act, so it always offers the next step. */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
            NeonOrb(icon)
            Spacer(Modifier.height(14.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
            NeonButton(text = actionLabel, onClick = onAction)
            Spacer(Modifier.height(6.dp))
        }
    }
}
