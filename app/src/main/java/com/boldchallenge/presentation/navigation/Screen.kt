package com.boldchallenge.presentation.navigation

/**
 * Sealed class defining all navigation destinations.
 * Each screen has a unique route for Navigation Compose.
 */
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Search : Screen("search")
    data class Forecast(val locationName: String) : Screen("forecast/{locationName}") {
        /** Create route with location name parameter */
        fun createRoute(locationName: String): String = "forecast/$locationName"

        companion object {
            const val ROUTE = "forecast/{locationName}"
        }
    }
}

