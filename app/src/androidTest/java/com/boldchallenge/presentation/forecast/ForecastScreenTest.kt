package com.boldchallenge.presentation.forecast

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.domain.error.AppError
import com.domain.model.Forecast
import com.domain.model.ForecastDay
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ForecastScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun given_loading_state_when_screen_is_rendered_then_shows_loading_content() {
            // Given
            val uiState = ForecastUiState(isLoading = true)

            // When
            composeTestRule.setContent {
                ForecastScreen(
                    uiState = uiState,
                    onRetry = {},
                    onRefresh = {},
                    onNavigateBack = {}
                )
            }

            // Then
            composeTestRule.onNodeWithText("Loading forecast...").assertIsDisplayed()
        }

    @Test
    fun given_error_state_when_screen_is_rendered_then_shows_error_content() {
            // Given
            val uiState = ForecastUiState(
                error = AppError.NetworkError.NoConnection
            )

            // When
            composeTestRule.setContent {
                ForecastScreen(
                    uiState = uiState,
                    onRetry = {},
                    onRefresh = {},
                    onNavigateBack = {}
                )
            }

            // Then
            composeTestRule.onNodeWithText("Couldn't load forecast").assertIsDisplayed()
            composeTestRule.onNodeWithText("Check your connection and try again").assertIsDisplayed()
            composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
        }

    @Test
    fun given_success_state_with_forecast_when_screen_is_rendered_then_shows_forecast_content() {
            // Given
            val forecast = createForecast("Bogotá")
            val uiState = ForecastUiState(forecast = forecast)

            // When
            composeTestRule.setContent {
                ForecastScreen(
                    uiState = uiState,
                    onRetry = {},
                    onRefresh = {},
                    onNavigateBack = {}
                )
            }

            // Then
            composeTestRule.onNodeWithText("Bogotá").assertIsDisplayed()
            composeTestRule.onNodeWithText("Today").assertIsDisplayed()
            composeTestRule.onNodeWithText("Upcoming days").assertIsDisplayed()
        }

    @Test
    fun given_refreshing_state_when_screen_is_rendered_then_shows_refresh_indicator() {
            // Given
            val forecast = createForecast("Bogotá")
            val uiState = ForecastUiState(
                forecast = forecast,
                isRefreshing = true
            )

            // When
            composeTestRule.setContent {
                ForecastScreen(
                    uiState = uiState,
                    onRetry = {},
                    onRefresh = {},
                    onNavigateBack = {}
                )
            }

            // Then
            // Refresh indicator should be visible (CircularProgressIndicator in refresh button)
            composeTestRule.onNodeWithText("Bogotá").assertIsDisplayed()
            composeTestRule.onNodeWithContentDescription("Refresh").assertIsDisplayed()
        }

    @Test
    fun given_error_state_when_user_clicks_retry_button_then_calls_onRetry() {
            // Given
            var retryCalled = false
            val uiState = ForecastUiState(
                error = AppError.NetworkError.NoConnection
            )

            // When
            composeTestRule.setContent {
                ForecastScreen(
                    uiState = uiState,
                    onRetry = { retryCalled = true },
                    onRefresh = {},
                    onNavigateBack = {}
                )
            }

            composeTestRule.onNodeWithText("Retry").performClick()

            // Then
            assertTrue(retryCalled)
        }

    @Test
    fun given_forecast_content_when_user_clicks_refresh_button_then_calls_onRefresh() {
            // Given
            var refreshCalled = false
            val forecast = createForecast("Bogotá")
            val uiState = ForecastUiState(forecast = forecast)

            // When
            composeTestRule.setContent {
                ForecastScreen(
                    uiState = uiState,
                    onRetry = {},
                    onRefresh = { refreshCalled = true },
                    onNavigateBack = {}
                )
            }

            composeTestRule.onNodeWithContentDescription("Refresh").performClick()

            // Then
            assertTrue(refreshCalled)
        }

    @Test
    fun given_forecast_content_when_user_clicks_back_button_then_calls_onNavigateBack() {
            // Given
            var backCalled = false
            val forecast = createForecast("Bogotá")
            val uiState = ForecastUiState(forecast = forecast)

            // When
            composeTestRule.setContent {
                ForecastScreen(
                    uiState = uiState,
                    onRetry = {},
                    onRefresh = {},
                    onNavigateBack = { backCalled = true }
                )
            }

            composeTestRule.onNodeWithContentDescription("Back").performClick()

            // Then
            assertTrue(backCalled)
        }

    @Test
    fun given_forecast_when_rendered_then_displays_location_name_in_header() {
            // Given
            val forecast = createForecast("Bogotá")
            val uiState = ForecastUiState(forecast = forecast)

            // When
            composeTestRule.setContent {
                ForecastScreen(
                    uiState = uiState,
                    onRetry = {},
                    onRefresh = {},
                    onNavigateBack = {}
                )
            }

            // Then
            composeTestRule.onNodeWithText("Bogotá").assertIsDisplayed()
            composeTestRule.onNodeWithText("3-day forecast").assertIsDisplayed()
        }

    @Test
    fun given_forecast_when_rendered_then_displays_today_card_with_temperature() {
            // Given
            val forecast = createForecast("Bogotá")
            val uiState = ForecastUiState(forecast = forecast)

            // When
            composeTestRule.setContent {
                ForecastScreen(
                    uiState = uiState,
                    onRetry = {},
                    onRefresh = {},
                    onNavigateBack = {}
                )
            }

            // Then
            composeTestRule.onNodeWithText("Today").assertIsDisplayed()
            // Temperature should be displayed (18° from createForecast)
            composeTestRule.onNodeWithText("18°").assertIsDisplayed()
        }

    @Test
    fun given_forecast_when_rendered_then_displays_upcoming_days_section() {
            // Given
            val forecast = createForecast("Bogotá")
            val uiState = ForecastUiState(forecast = forecast)

            // When
            composeTestRule.setContent {
                ForecastScreen(
                    uiState = uiState,
                    onRetry = {},
                    onRefresh = {},
                    onNavigateBack = {}
                )
            }

            // Then
            composeTestRule.onNodeWithText("Upcoming days").assertIsDisplayed()
            // Should display day names (Monday, Tuesday, etc.)
        }

    // Helper functions
    private fun createForecast(locationName: String): Forecast = Forecast(
        locationName = locationName,
        days = listOf(
            ForecastDay(
                date = "2024-01-15",
                avgTempC = 18.5,
                conditionText = "Partly cloudy",
                conditionIconUrl = "https://cdn.weatherapi.com/weather/64x64/day/116.png"
            ),
            ForecastDay(
                date = "2024-01-16",
                avgTempC = 20.0,
                conditionText = "Sunny",
                conditionIconUrl = "https://cdn.weatherapi.com/weather/64x64/day/113.png"
            ),
            ForecastDay(
                date = "2024-01-17",
                avgTempC = 16.8,
                conditionText = "Light rain",
                conditionIconUrl = "https://cdn.weatherapi.com/weather/64x64/day/296.png"
            )
        )
    )
}

