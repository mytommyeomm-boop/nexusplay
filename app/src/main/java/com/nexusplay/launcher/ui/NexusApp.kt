package com.nexusplay.launcher.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.nexusplay.launcher.ui.components.NeonBackground
import com.nexusplay.launcher.ui.components.SplashScreen
import com.nexusplay.launcher.ui.navigation.AppNavigation
import kotlinx.coroutines.delay

@Composable
fun NexusApp() {
    // rememberSaveable so rotating the phone does not replay the splash.
    var booting by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(booting) {
        if (booting) {
            delay(1800)
            booting = false
        }
    }

    NeonBackground {
        AnimatedVisibility(
            visible = !booting,
            enter = fadeIn(tween(500)),
            exit = fadeOut(tween(200))
        ) {
            AppNavigation()
        }
        AnimatedVisibility(
            visible = booting,
            enter = fadeIn(tween(0)),
            exit = fadeOut(tween(400))
        ) {
            SplashScreen()
        }
    }
}
