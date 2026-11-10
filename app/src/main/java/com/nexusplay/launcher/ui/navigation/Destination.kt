package com.nexusplay.launcher.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.nexusplay.launcher.R
import com.nexusplay.launcher.ui.components.NeonIcons

/** The five top-level tabs, in bottom-bar order. */
enum class Destination(val route: String, @StringRes val labelRes: Int, val icon: ImageVector) {
    Home("home", R.string.nav_home, NeonIcons.Home),
    Library("library", R.string.nav_library, NeonIcons.Library),
    Import("import", R.string.nav_import, NeonIcons.Import),
    Manager("manager", R.string.nav_manager, NeonIcons.Folder),
    Settings("settings", R.string.nav_settings, NeonIcons.Sliders)
}

/** Sub-screen of Settings; not a tab. */
const val JOYSTICK_ROUTE = "settings/joystick"
const val RUNTIMES_ROUTE = "settings/runtimes"
