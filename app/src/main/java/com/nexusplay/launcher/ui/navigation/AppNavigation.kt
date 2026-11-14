package com.nexusplay.launcher.ui.navigation

import com.nexusplay.launcher.ui.screens.MascotLabScreen
import com.nexusplay.launcher.ui.screens.MascotGalleryScreen
import kotlinx.coroutines.delay
import com.nexusplay.launcher.ui.screens.MascotSettingsScreen
import com.nexusplay.launcher.mascot.LocalMascot
import androidx.compose.runtime.LaunchedEffect
import com.nexusplay.launcher.ui.screens.LanguageSettingsScreen
import androidx.compose.animation.AnimatedContentTransitionScope
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
import com.nexusplay.launcher.ui.components.DeckNavBar
import com.nexusplay.launcher.ui.screens.HomeScreen
import com.nexusplay.launcher.ui.screens.ImportScreen
import com.nexusplay.launcher.ui.screens.JoystickSettingsScreen
import com.nexusplay.launcher.ui.screens.LibraryScreen
import com.nexusplay.launcher.ui.screens.ManagerScreen
import com.nexusplay.launcher.ui.screens.RuntimeManagerScreen
import com.nexusplay.launcher.ui.screens.SettingsScreen
import com.nexusplay.launcher.ui.theme.TextPrimary

private val tabOrder = Destination.entries.map { it.route }

/** +1 when moving to a tab on the right, -1 when moving left, so screens slide with the rail. */
private fun AnimatedContentTransitionScope<NavBackStackEntry>.direction(): Int {
    val from = tabOrder.indexOf(initialState.destination.route)
    val to = tabOrder.indexOf(targetState.destination.route)
    if (to == -1) return 1      // opening a sub-screen: slide in from the right
    if (from == -1) return -1   // coming back from a sub-screen
    return if (to >= from) 1 else -1
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val mascot = LocalMascot.current

    // The joystick screen belongs to Settings, so the Settings key stays lit while it is open.
    val inSettingsSub = currentRoute in setOf(
        JOYSTICK_ROUTE, RUNTIMES_ROUTE, LANGUAGE_ROUTE, MASCOT_ROUTE, MASCOT_GALLERY_ROUTE, MASCOT_LAB_ROUTE
    )
    val selectedRoute = if (inSettingsSub) Destination.Settings.route else currentRoute

    val navigateTo: (Destination) -> Unit = { destination ->
        if (destination == Destination.Settings && inSettingsSub) {
            navController.popBackStack(Destination.Settings.route, inclusive = false)
        } else {
            navController.navigate(destination.route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    // A tour was requested (suggestion banner, welcome screen or Settings > Mascot): go to its screen, then start it.
    val pending = mascot.pendingTutorial
    LaunchedEffect(pending) {
        val tutorial = pending ?: return@LaunchedEffect
        if (currentRoute != tutorial.route) {
            val tab = Destination.entries.firstOrNull { it.route == tutorial.route }
            if (tab != null) navigateTo(tab) else navController.navigate(tutorial.route) { launchSingleTop = true }
            delay(500)
        } else {
            delay(150)
        }
        mascot.beginPending()
    }

    // Offer a tour once per screen, after the screen has settled.
    LaunchedEffect(currentRoute, mascot.welcomeVisible) {
        if (currentRoute != null && !mascot.welcomeVisible) {
            delay(1400)
            mascot.suggestFor(currentRoute)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = TextPrimary,
        bottomBar = {
            DeckNavBar(
                items = Destination.entries,
                currentRoute = selectedRoute,
                onSelect = navigateTo
            )
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Home.route,
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                val d = direction()
                slideInHorizontally(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    initialOffsetX = { it * d / 8 }
                ) + fadeIn(tween(240))
            },
            exitTransition = {
                val d = direction()
                slideOutHorizontally(
                    animationSpec = tween(220, easing = FastOutSlowInEasing),
                    targetOffsetX = { -it * d / 8 }
                ) + fadeOut(tween(140))
            },
            popEnterTransition = {
                val d = direction()
                slideInHorizontally(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    initialOffsetX = { it * d / 8 }
                ) + fadeIn(tween(240))
            },
            popExitTransition = {
                val d = direction()
                slideOutHorizontally(
                    animationSpec = tween(220, easing = FastOutSlowInEasing),
                    targetOffsetX = { -it * d / 8 }
                ) + fadeOut(tween(140))
            }
        ) {
            composable(Destination.Home.route) { HomeScreen(padding, navigateTo) }
            composable(Destination.Library.route) { LibraryScreen(padding, navigateTo) }
            composable(Destination.Import.route) { ImportScreen(padding) }
            composable(Destination.Manager.route) { ManagerScreen(padding, navigateTo) }
            composable(Destination.Settings.route) {
                SettingsScreen(
                    padding,
                    onOpenLanguage = { navController.navigate(LANGUAGE_ROUTE) { launchSingleTop = true } },
                    onOpenMascot = { navController.navigate(MASCOT_ROUTE) { launchSingleTop = true } },
                    onOpenJoystick = { navController.navigate(JOYSTICK_ROUTE) { launchSingleTop = true } },
                    onOpenRuntimes = { navController.navigate(RUNTIMES_ROUTE) { launchSingleTop = true } }
                )
            }
            composable(MASCOT_ROUTE) {
                MascotSettingsScreen(
                    padding,
                    onBack = { navController.popBackStack() },
                    onOpenGallery = { navController.navigate(MASCOT_GALLERY_ROUTE) { launchSingleTop = true } },
                    onOpenLab = { navController.navigate(MASCOT_LAB_ROUTE) { launchSingleTop = true } }
                )
            }
            composable(MASCOT_GALLERY_ROUTE) {
                MascotGalleryScreen(padding, onBack = { navController.popBackStack() })
            }
            composable(MASCOT_LAB_ROUTE) {
                MascotLabScreen(padding, onBack = { navController.popBackStack() })
            }
            composable(LANGUAGE_ROUTE) {
                LanguageSettingsScreen(padding, onBack = { navController.popBackStack() })
            }
            composable(RUNTIMES_ROUTE) {
                RuntimeManagerScreen(padding, onBack = { navController.popBackStack() })
            }
            composable(JOYSTICK_ROUTE) {
                JoystickSettingsScreen(padding, onBack = { navController.popBackStack() })
            }
        }
    }
}
