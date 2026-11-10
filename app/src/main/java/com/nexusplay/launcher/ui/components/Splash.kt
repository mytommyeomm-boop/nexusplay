package com.nexusplay.launcher.ui.components

import androidx.compose.ui.res.stringResource
import com.nexusplay.launcher.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nexusplay.launcher.core.DeviceInfo
import com.nexusplay.launcher.core.DeviceInfoReader
import com.nexusplay.launcher.ui.theme.Titanium
import kotlinx.coroutines.delay

/** Boot screen. The three readouts are real values read from this device. */
@Composable
fun SplashScreen(device: DeviceInfo, modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    var lines by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(1500, easing = LinearOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        repeat(3) {
            delay(380)
            lines++
        }
    }

    val readouts = listOf(
        stringResource(R.string.splash_architecture) to device.primaryAbi,
        stringResource(R.string.splash_memory) to DeviceInfoReader.formatGb(device.totalRamBytes),
        stringResource(R.string.vulkan_label) to stringResource(if (device.hasVulkan) R.string.vulkan_yes else R.string.vulkan_no)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        LogoMark(64.dp)
        Spacer(Modifier.height(20.dp))
        Text("NexusPlay", style = MaterialTheme.typography.displaySmall)
        Text(stringResource(R.string.tagline), style = MaterialTheme.typography.bodyMedium, color = Titanium)
        Spacer(Modifier.height(36.dp))
        SegmentBar(fraction = { progress.value })
        Spacer(Modifier.height(18.dp))
        readouts.forEachIndexed { index, (label, value) ->
            AnimatedVisibility(
                visible = lines > index,
                enter = fadeIn(tween(200)) + expandVertically(tween(200))
            ) {
                Readout(label, value)
            }
        }
    }
}
