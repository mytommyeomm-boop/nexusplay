package com.nexusplay.launcher.ui.components

import com.nexusplay.launcher.ui.mascot.tutorialAnchor
import com.nexusplay.launcher.mascot.Anchor
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexusplay.launcher.ui.navigation.Destination
import com.nexusplay.launcher.ui.theme.Graphite1
import com.nexusplay.launcher.ui.theme.Graphite2
import com.nexusplay.launcher.ui.theme.Graphite3
import com.nexusplay.launcher.ui.theme.Lime
import com.nexusplay.launcher.ui.theme.Titanium

/**
 * Edge-to-edge hardware strip with five keys. A single lime rail slides between keys and
 * the active key sits in a shallow recess. Each key is 68dp tall and at least 60dp wide.
 */
@Composable
fun DeckNavBar(
    items: List<Destination>,
    currentRoute: String?,
    onSelect: (Destination) -> Unit
) {
    val selectedIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
    val position by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 380f),
        label = "rail"
    )
    val shape = ChamferShape(22.dp, topLeft = true)

    Box(
        Modifier
            .fillMaxWidth()
            .tutorialAnchor(Anchor.NavBar)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Graphite2, Graphite1)))
            .border(
                1.dp,
                Brush.linearGradient(listOf(Titanium.copy(alpha = 0.5f), Graphite3)),
                shape
            )
    ) {
        Column(Modifier.navigationBarsPadding()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .drawBehind {
                        val keyW = size.width / items.size
                        val x = position * keyW
                        drawRect(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.38f), Color.Transparent),
                                startY = 0f,
                                endY = size.height
                            ),
                            topLeft = Offset(x + 4.dp.toPx(), 8.dp.toPx()),
                            size = Size(keyW - 8.dp.toPx(), size.height - 14.dp.toPx())
                        )
                        for (i in 1 until items.size) {
                            drawLine(
                                Color.Black.copy(alpha = 0.35f),
                                Offset(i * keyW, 16.dp.toPx()),
                                Offset(i * keyW, size.height - 12.dp.toPx()),
                                1f
                            )
                        }
                        drawRect(
                            Lime,
                            Offset(x + keyW / 2f - 12.dp.toPx(), 5.dp.toPx()),
                            Size(24.dp.toPx(), 3.dp.toPx())
                        )
                    }
            ) {
                items.forEachIndexed { index, destination ->
                    DeckNavKey(
                        destination = destination,
                        selected = index == selectedIndex,
                        modifier = Modifier.weight(1f),
                        onClick = { onSelect(destination) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DeckNavKey(
    destination: Destination,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val tint by animateColorAsState(if (selected) Lime else Titanium, tween(180), label = "keyTint")
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 700f),
        label = "keyScale"
    )
    val sink by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "keySink"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(interactionSource = interaction, indication = null, role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = destination.icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationY = sink * 1.5.dp.toPx()
                }
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(destination.labelRes),
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp),
            color = tint,
            maxLines = 1,
            modifier = Modifier.graphicsLayer { translationY = sink * 1.5.dp.toPx() }
        )
    }
}
