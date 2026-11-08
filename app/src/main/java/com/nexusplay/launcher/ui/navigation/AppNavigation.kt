package com.nexusplay.launcher.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nexusplay.launcher.ui.components.HwNavDock
import com.nexusplay.launcher.ui.screens.HomeScreen
import com.nexusplay.launcher.ui.screens.ImportScreen
import com.nexusplay.launcher.ui.screens.JoystickSettingsScreen
import com.nexusplay.launcher.ui.screens.LibraryScreen
import com.nexusplay.launcher.ui.screens.ManagerScreen
import com.nexusplay.launcher.ui.screens.SettingsScreen
import com.nexusplay.launcher.ui.theme.TextPrimary

const val JOYSTICK_ROUTE = "joystick"

// Sub-screens (not in the tab strip) count as "deeper" than every tab.
private fun tabIndex(route: String?) =
    Destination.entries.indexOfFirst { it.route == route }.let { if (it < 0) 99 else it }

/** Direction-aware shift: content slides the way the tab strip is travelling. Short, cheap, no scaling. */
private fun AnimatedContentTransitionScope<NavBackStackEntry>.direction(): Int =
    if (tabIndex(targetState.destination.route) >= tabIndex(initialState.destination.route)) 1 else -1

private val tabEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    val dir = direction()
    fadeIn(tween(240)) + slideInHorizontally(tween(280, easing = FastOutSlowInEasing)) { dir * it / 14 }
}

private val tabExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    val dir = direction()
    fadeOut(tween(120)) + slideOutHorizontally(tween(200, easing = FastOutSlowInEasing)) { -dir * it / 14 }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val navigateTo: (Destination) -> Unit = { destination ->
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = TextPrimary,
        bottomBar = {
            HwNavDock(
                items = Destination.entries,
                currentRoute = currentRoute,
                onSelect = navigateTo
            )
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Home.route,
            modifier = Modifier.fillMaxSize(),
            enterTransition = tabEnter,
            exitTransition = tabExit,
            popEnterTransition = tabEnter,
            popExitTransition = tabExit
        ) {
            composable(Destination.Home.route) { HomeScreen(padding, navigateTo) }
            composable(Destination.Library.route) { LibraryScreen(padding, navigateTo) }
            composable(Destination.Import.route) { ImportScreen(padding) }
            composable(Destination.Manager.route) { ManagerScreen(padding, navigateTo) }
            composable(Destination.Settings.route) { SettingsScreen(padding) { navController.navigate(JOYSTICK_ROUTE) } }
            composable(JOYSTICK_ROUTE) { JoystickSettingsScreen(padding) { navController.popBackStack() } }
        }
    }
}
