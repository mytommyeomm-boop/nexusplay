package com.nexusplay.launcher.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.nexusplay.launcher.ui.components.NeonIcons

/** The five top-level tabs, in bottom-bar order. */
enum class Destination(val route: String, val label: String, val icon: ImageVector) {
    Home("home", "Home", NeonIcons.Home),
    Library("library", "Library", NeonIcons.Library),
    Import("import", "Import", NeonIcons.Import),
    Manager("manager", "Manager", NeonIcons.Folder),
    Settings("settings", "Settings", NeonIcons.Sliders)
}

/** Sub-screen of Settings; not a tab. */
const val JOYSTICK_ROUTE = "settings/joystick"
const val RUNTIMES_ROUTE = "settings/runtimes"
