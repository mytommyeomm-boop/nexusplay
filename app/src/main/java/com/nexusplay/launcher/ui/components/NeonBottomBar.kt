package com.nexusplay.launcher.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.nexusplay.launcher.ui.navigation.Destination
import com.nexusplay.launcher.ui.theme.NeonBlue
import com.nexusplay.launcher.ui.theme.NeonCyan
import com.nexusplay.launcher.ui.theme.NeonGradient
import com.nexusplay.launcher.ui.theme.NeonPurple
import com.nexusplay.launcher.ui.theme.TextMuted

/** Floating glass navigation bar. Every tab is a full-height 66dp touch target. */
@Composable
fun NeonBottomBar(
    items: List<Destination>,
    currentRoute: String?,
    onSelect: (Destination) -> Unit
) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .shadow(18.dp, shape, ambientColor = NeonBlue, spotColor = NeonCyan)
                .clip(shape)
                .background(Color(0xEB0B0F24))
                .border(
                    1.dp,
                    Brush.linearGradient(
                        listOf(NeonCyan.copy(alpha = 0.5f), NeonPurple.copy(alpha = 0.2f), NeonCyan.copy(alpha = 0.1f))
                    ),
                    shape
                )
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { destination ->
                BarItem(
                    destination = destination,
                    selected = destination.route == currentRoute,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelect(destination) }
                )
            }
        }
    }
}

@Composable
private fun BarItem(
    destination: Destination,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val tint by animateColorAsState(if (selected) NeonCyan else TextMuted, tween(220), label = "tint")
    val glow by animateFloatAsState(if (selected) 1f else 0f, tween(300), label = "glow")
    val lift by animateFloatAsState(
        targetValue = if (selected) -2f else 0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 300f),
        label = "lift"
    )
    val indicator by animateDpAsState(
        targetValue = if (selected) 26.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 350f),
        label = "indicator"
    )

    Column(
        modifier = modifier
            .height(66.dp)
            .clickable(interactionSource = interaction, indication = null, role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(
            Modifier
                .height(3.dp)
                .width(indicator)
                .clip(CircleShape)
                .background(NeonGradient)
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(34.dp)
                .drawBehind {
                    drawCircle(
                        Brush.radialGradient(
                            listOf(NeonCyan.copy(alpha = 0.35f * glow), Color.Transparent)
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer { translationY = lift * density }
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = destination.label,
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            maxLines = 1
        )
    }
}
