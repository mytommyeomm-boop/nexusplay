package com.nexusplay.launcher.ui.navigation

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
import com.nexusplay.launcher.ui.screens.LibraryScreen
import com.nexusplay.launcher.ui.screens.ManagerScreen
import com.nexusplay.launcher.ui.screens.SettingsScreen
import com.nexusplay.launcher.ui.theme.TextPrimary

private val tabOrder = Destination.entries.map { it.route }

/** +1 when moving to a tab on the right, -1 when moving left, so screens slide with the rail. */
private fun AnimatedContentTransitionScope<NavBackStackEntry>.direction(): Int {
    val from = tabOrder.indexOf(initialState.destination.route)
    val to = tabOrder.indexOf(targetState.destination.route)
    return if (to >= from) 1 else -1
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
            DeckNavBar(
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
            composable(Destination.Settings.route) { SettingsScreen(padding) }
        }
    }
}
