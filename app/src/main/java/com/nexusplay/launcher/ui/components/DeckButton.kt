package com.nexusplay.launcher.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.nexusplay.launcher.ui.theme.Graphite0
import com.nexusplay.launcher.ui.theme.Graphite2
import com.nexusplay.launcher.ui.theme.Graphite3
import com.nexusplay.launcher.ui.theme.Lime
import com.nexusplay.launcher.ui.theme.LimeDeep
import com.nexusplay.launcher.ui.theme.TextMuted

/**
 * Keycap-style primary action. The face travels 4dp down over a darker edge when pressed.
 * Always fills the width it is given, which keeps it an easy thumb target (53dp face).
 */
@Composable
fun DeckButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val travel by animateFloatAsState(
        targetValue = if (pressed && enabled) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 800f),
        label = "keyTravel"
    )
    val shape = ChamferShape(12.dp, topLeft = true, bottomRight = true)
    val face = if (enabled) Lime else Graphite2
    val edge = if (enabled) LimeDeep else Graphite0
    val foreground = if (enabled) Graphite0 else TextMuted

    Box(modifier.fillMaxWidth().height(57.dp)) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(top = 4.dp)
                .clip(shape)
                .background(edge)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(53.dp)
                .graphicsLayer { translationY = travel * 4.dp.toPx() }
                .clip(shape)
                .background(face)
                .then(if (enabled) Modifier else Modifier.border(1.dp, Graphite3, shape))
                .drawBehind {
                    if (enabled) {
                        drawLine(
                            Color.White.copy(alpha = 0.35f),
                            Offset(12.dp.toPx(), 1f),
                            Offset(size.width, 1f),
                            2f
                        )
                    }
                }
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick
                )
                .padding(horizontal = 20.dp),
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
}
