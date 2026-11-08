package com.nexusplay.launcher.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.nexusplay.launcher.ui.theme.NeonBlue
import com.nexusplay.launcher.ui.theme.NeonCyan
import com.nexusplay.launcher.ui.theme.NeonGradient
import com.nexusplay.launcher.ui.theme.NeonPurple
import com.nexusplay.launcher.ui.theme.Obsidian
import com.nexusplay.launcher.ui.theme.TextMuted

/** Primary call-to-action: gradient pill, 52dp minimum height for comfortable touch. */
@Composable
fun NeonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 500f),
        label = "buttonPress"
    )
    val shape = RoundedCornerShape(16.dp)
    val foreground = if (enabled) Obsidian else TextMuted
    val background: Brush = if (enabled) NeonGradient
    else Brush.horizontalGradient(listOf(Color(0xFF171C38), Color(0xFF171C38)))

    Row(
        modifier = modifier
            .heightIn(min = 52.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .then(
                if (enabled) Modifier.shadow(14.dp, shape, ambientColor = NeonBlue, spotColor = NeonCyan)
                else Modifier
            )
            .clip(shape)
            .background(background)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 22.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = foreground, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, color = foreground)
    }
}

/** Small status pill, e.g. file formats or "Soon" markers. */
@Composable
fun StatusTag(text: String, color: Color = NeonPurple, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        maxLines = 1,
        modifier = modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.35f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}
