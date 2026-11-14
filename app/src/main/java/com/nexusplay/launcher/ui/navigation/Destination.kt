package com.nexusplay.launcher.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.nexusplay.launcher.R
import com.nexusplay.launcher.ui.components.NeonIcons

const val ROUTE_HOME = "home"
const val ROUTE_IMPORT = "import"
const val ROUTE_SETTINGS = "settings"

/** The five top-level tabs, in bottom-bar order. */
enum class Destination(val route: String, @StringRes val labelRes: Int, val icon: ImageVector) {
    Home(ROUTE_HOME, R.string.nav_home, NeonIcons.Home),
    Library("library", R.string.nav_library, NeonIcons.Library),
    Import(ROUTE_IMPORT, R.string.nav_import, NeonIcons.Import),
    Manager("manager", R.string.nav_manager, NeonIcons.Folder),
    Settings(ROUTE_SETTINGS, R.string.nav_settings, NeonIcons.Sliders)
}

/** Sub-screen of Settings; not a tab. */
const val JOYSTICK_ROUTE = "settings/joystick"
const val LANGUAGE_ROUTE = "settings/language"
const val RUNTIMES_ROUTE = "settings/runtimes"
const val MASCOT_ROUTE = "settings/mascot"
const val MASCOT_GALLERY_ROUTE = "settings/mascot/gallery"
const val MASCOT_LAB_ROUTE = "settings/mascot/lab"
