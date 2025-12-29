package com.boldchallenge.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.boldchallenge.presentation.forecast.ForecastRoute
import com.boldchallenge.presentation.search.SearchRoute
import com.boldchallenge.presentation.splash.SplashScreen

/**
 * Main Navigation Graph for the app.
 * Routes connect ViewModels to Screens; SplashScreen is pure UI without ViewModel.
 */
@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash - Pure UI, no ViewModel
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Search - Route connects ViewModel to Screen
        composable(Screen.Search.route) {
            SearchRoute(
                onLocationSelected = { locationName ->
                    navController.navigate(Screen.Forecast("").createRoute(locationName))
                }
            )
        }

        // Forecast - Route connects ViewModel to Screen
        composable(
            route = Screen.Forecast.ROUTE,
            arguments = listOf(
                navArgument("locationName") { type = NavType.StringType }
            )
        ) {
            ForecastRoute(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

