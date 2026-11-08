package com.nexusplay.launcher.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.nexusplay.launcher.ui.navigation.Destination
import com.nexusplay.launcher.ui.theme.AccentLime
import com.nexusplay.launcher.ui.theme.Graphite1
import com.nexusplay.launcher.ui.theme.Graphite2
import com.nexusplay.launcher.ui.theme.Hairline
import com.nexusplay.launcher.ui.theme.TextMuted
import com.nexusplay.launcher.ui.theme.Titanium

/**
 * Docked key strip. Full width, one chamfered corner, no floating pill.
 * Each key is 56dp tall and a full-width-share touch target.
 */
@Composable
fun HwNavDock(
    items: List<Destination>,
    currentRoute: String?,
    onSelect: (Destination) -> Unit
) {
    val shape = ChamferShape(topRight = 24.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Graphite2, Graphite1)))
            .border(1.dp, Brush.verticalGradient(listOf(Titanium.copy(alpha = 0.45f), Hairline)), shape)
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { destination ->
            DockKey(
                destination = destination,
                selected = destination.route == currentRoute,
                modifier = Modifier.weight(1f),
                onClick = { onSelect(destination) }
            )
        }
    }
}

@Composable
private fun DockKey(
    destination: Destination,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val tint by animateColorAsState(if (selected) AccentLime else Titanium, tween(180), label = "dockTint")
    val bar by animateFloatAsState(if (selected) 1f else 0f, spring(dampingRatio = 0.7f, stiffness = 350f), label = "dockBar")
    val press by animateFloatAsState(if (pressed) 1f else 0f, tween(80), label = "dockPress")

    Column(
        modifier = modifier
            .height(56.dp)
            .graphicsLayer { translationY = press * 2f * density }
            .drawBehind {
                if (bar > 0f) {
                    drawRect(AccentLime.copy(alpha = 0.06f * bar))
                    val w = size.width * 0.5f * bar
                    drawRect(AccentLime, Offset((size.width - w) / 2f, 0f), Size(w, 2.dp.toPx()))
                }
            }
            .clickable(interactionSource = interaction, indication = null, role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(destination.icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(3.dp))
        Text(
            destination.label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) tint else TextMuted,
            maxLines = 1
        )
    }
}
