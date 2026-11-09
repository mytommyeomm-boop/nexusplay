package com.nexusplay.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nexusplay.launcher.ui.theme.Cyan
import com.nexusplay.launcher.ui.theme.Titanium
import com.nexusplay.launcher.ui.theme.TitaniumLight

/** Left-aligned title, optional live readout on the right, rule underneath. */
@Composable
fun ScreenHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    readout: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(title, style = MaterialTheme.typography.displaySmall, modifier = Modifier.weight(1f))
            if (readout != null) {
                Text(
                    readout,
                    style = MaterialTheme.typography.labelSmall,
                    color = Cyan,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        HeaderRule()
        Spacer(Modifier.height(8.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Titanium)
    }
}

/** Section label that runs into a hairline, like a panel legend. */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, color = TitaniumLight)
        Spacer(Modifier.width(12.dp))
        Spacer(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(Titanium.copy(alpha = 0.2f))
        )
    }
}
