package com.nexusplay.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.nexusplay.launcher.ui.theme.NeonCyan
import com.nexusplay.launcher.ui.theme.NeonPurple
import com.nexusplay.launcher.ui.theme.TextPrimary
import com.nexusplay.launcher.ui.theme.TextSecondary

/** Gradient headline. BasicText is used because Material Text overrides a brush with its color. */
@Composable
fun GradientTitle(text: String, modifier: Modifier = Modifier) {
    BasicText(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.displaySmall.copy(
            brush = Brush.horizontalGradient(listOf(TextPrimary, NeonCyan, NeonPurple))
        )
    )
}

@Composable
fun ScreenHeader(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .staggeredEntrance(0),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        GradientTitle(title)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(
            Modifier
                .width(3.dp)
                .height(16.dp)
                .clip(CircleShape)
                .background(Brush.verticalGradient(listOf(NeonCyan, NeonPurple)))
        )
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
    }
}
