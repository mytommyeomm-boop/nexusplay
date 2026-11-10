package com.nexusplay.launcher

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.nexusplay.launcher.input.JoystickSettingsHolder
import com.nexusplay.launcher.input.JoystickSettingsStore
import com.nexusplay.launcher.runtime.RuntimeSelection
import com.nexusplay.launcher.ui.NexusApp
import com.nexusplay.launcher.ui.theme.NexusTheme

class MainActivity : ComponentActivity() {
    private lateinit var joystick: JoystickSettingsHolder
    private var resumeTick by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        RuntimeSelection.load(this)
        joystick = JoystickSettingsHolder(applicationContext, JoystickSettingsStore.load(this))
        setContent {
            NexusTheme {
                NexusApp(joystick, resumeTick)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        resumeTick++
    }

    override fun onStop() {
        // Never lose a change just because the user left the app right after making it.
        joystick.flush()
        super.onStop()
    }
}
