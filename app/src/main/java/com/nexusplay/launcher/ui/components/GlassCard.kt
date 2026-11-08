package com.nexusplay.launcher.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.nexusplay.launcher.ui.theme.NeonCyan
import com.nexusplay.launcher.ui.theme.NeonPurple

/** Frosted panel with a neon edge. Springs down slightly while pressed when clickable. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    accent: Color = NeonCyan,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "cardPress"
    )

    val base = modifier
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clip(shape)
        .background(
            Brush.verticalGradient(
                listOf(Color.White.copy(alpha = 0.09f), Color.White.copy(alpha = 0.03f))
            )
        )
        .border(
            1.dp,
            Brush.linearGradient(listOf(accent.copy(alpha = 0.55f), NeonPurple.copy(alpha = 0.12f))),
            shape
        )

    val interactive = if (onClick != null) {
        base.clickable(
            interactionSource = interaction,
            indication = null,
            role = Role.Button,
            onClick = onClick
        )
    } else base

    Column(interactive.padding(16.dp), content = content)
}
