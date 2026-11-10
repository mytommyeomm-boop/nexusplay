package com.nexusplay.launcher.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.nexusplay.launcher.core.DeviceInfoReader
import com.nexusplay.launcher.core.LocalDeviceInfo
import com.nexusplay.launcher.input.JoystickSettingsHolder
import com.nexusplay.launcher.input.LocalJoystickSettings
import com.nexusplay.launcher.ui.components.DeckBackground
import com.nexusplay.launcher.ui.components.SplashScreen
import com.nexusplay.launcher.ui.navigation.AppNavigation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun NexusApp(joystick: JoystickSettingsHolder) {
    val context = LocalContext.current
    val device = remember { DeviceInfoReader.read(context) }

    // Write settings to disk shortly after the last change; MainActivity.onStop flushes as well.
    LaunchedEffect(joystick.settings) {
        delay(250)
        withContext(Dispatchers.IO) { joystick.flush() }
    }

    // rememberSaveable so rotating the phone does not replay the boot screen.
    var booting by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(booting) {
        if (booting) {
            delay(1900)
            booting = false
        }
    }

    CompositionLocalProvider(
        LocalDeviceInfo provides device,
        LocalJoystickSettings provides joystick
    ) {
        DeckBackground {
            AnimatedVisibility(
                visible = !booting,
                enter = fadeIn(tween(450)),
                exit = fadeOut(tween(200))
            ) {
                AppNavigation()
            }
            AnimatedVisibility(
                visible = booting,
                enter = fadeIn(tween(0)),
                exit = fadeOut(tween(350))
            ) {
                SplashScreen(device)
            }
        }
    }
}
