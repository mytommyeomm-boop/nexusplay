package com.nexusplay.launcher.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nexusplay.launcher.ui.components.NeonBottomBar
import com.nexusplay.launcher.ui.screens.HomeScreen
import com.nexusplay.launcher.ui.screens.ImportScreen
import com.nexusplay.launcher.ui.screens.LibraryScreen
import com.nexusplay.launcher.ui.screens.ManagerScreen
import com.nexusplay.launcher.ui.screens.SettingsScreen
import com.nexusplay.launcher.ui.theme.TextPrimary

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
            NeonBottomBar(
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
                fadeIn(tween(260)) + slideInVertically(animationSpec = tween(300), initialOffsetY = { it / 28 })
            },
            exitTransition = { fadeOut(tween(140)) },
            popEnterTransition = {
                fadeIn(tween(260)) + slideInVertically(animationSpec = tween(300), initialOffsetY = { it / 28 })
            },
            popExitTransition = { fadeOut(tween(140)) }
        ) {
            composable(Destination.Home.route) { HomeScreen(padding, navigateTo) }
            composable(Destination.Library.route) { LibraryScreen(padding, navigateTo) }
            composable(Destination.Import.route) { ImportScreen(padding) }
            composable(Destination.Manager.route) { ManagerScreen(padding, navigateTo) }
            composable(Destination.Settings.route) { SettingsScreen(padding) }
        }
    }
}
