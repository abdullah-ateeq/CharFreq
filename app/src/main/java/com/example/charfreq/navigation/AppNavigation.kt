package com.example.charfreq.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.charfreq.ui.CharFreqViewModel
import com.example.charfreq.ui.about.AboutScreen
import com.example.charfreq.ui.history.HistoryScreen
import com.example.charfreq.ui.home.HomeScreen
import com.example.charfreq.ui.settings.SettingsScreen
import com.example.charfreq.ui.splash.SplashScreen

object Destinations {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
}

@Composable
fun AppNavigation(
    viewModel: CharFreqViewModel,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Destinations.SPLASH,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        composable(Destinations.SPLASH) {
            SplashScreen(
                onTimeout = {
                    navController.navigate(Destinations.HOME) {
                        popUpTo(Destinations.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Destinations.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToHistory = { navController.navigate(Destinations.HISTORY) },
                onNavigateToSettings = { navController.navigate(Destinations.SETTINGS) },
                onNavigateToAbout = { navController.navigate(Destinations.ABOUT) }
            )
        }

        composable(Destinations.HISTORY) {
            HistoryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.ABOUT) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
