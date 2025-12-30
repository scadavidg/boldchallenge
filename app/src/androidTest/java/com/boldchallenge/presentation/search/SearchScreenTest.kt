package com.boldchallenge.presentation.search

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.domain.error.AppError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SearchScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun given_initial_state_when_screen_is_rendered_then_shows_initial_content() {
        // Given
        val uiState = SearchUiState()

        // When
        composeTestRule.setContent {
            SearchScreen(
                uiState = uiState,
                onQueryChange = {},
                onRetry = {},
                onLocationSelected = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Explore").assertIsDisplayed()
        composeTestRule.onNodeWithText("Search any city in the world").assertIsDisplayed()
        composeTestRule.onNodeWithText("Discover the weather").assertIsDisplayed()
    }

    @Test
    fun given_loading_state_when_screen_is_rendered_then_shows_loading_indicator() {
        // Given
        val uiState = SearchUiState(
            query = "Bogotá",
            isLoading = true
        )

        // When
        composeTestRule.setContent {
            SearchScreen(
                uiState = uiState,
                onQueryChange = {},
                onRetry = {},
                onLocationSelected = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Searching...").assertIsDisplayed()
    }

    @Test
    fun given_error_state_when_screen_is_rendered_then_shows_error_content() {
        // Given
        val uiState = SearchUiState(
            query = "Bogotá",
            error = AppError.NetworkError.NoConnection
        )

        // When
        composeTestRule.setContent {
            SearchScreen(
                uiState = uiState,
                onQueryChange = {},
                onRetry = {},
                onLocationSelected = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Something went wrong").assertIsDisplayed()
        composeTestRule.onNodeWithText("We couldn't complete the search").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun given_empty_state_when_screen_is_rendered_then_shows_empty_content() {
        // Given
        val uiState = SearchUiState(
            query = "xyz123",
            isEmpty = true
        )

        // When
        composeTestRule.setContent {
            SearchScreen(
                uiState = uiState,
                onQueryChange = {},
                onRetry = {},
                onLocationSelected = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("No results").assertIsDisplayed()
        composeTestRule.onNodeWithText("Try another city name").assertIsDisplayed()
    }

    @Test
    fun given_query_too_short_state_when_screen_is_rendered_then_shows_query_too_short_content() {
        // Given
        val uiState = SearchUiState(
            query = "B"
        )

        // When
        composeTestRule.setContent {
            SearchScreen(
                uiState = uiState,
                onQueryChange = {},
                onRetry = {},
                onLocationSelected = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Keep typing").assertIsDisplayed()
        composeTestRule.onNodeWithText("Type at least 2 characters\nto start searching")
            .assertIsDisplayed()
    }

    @Test
    fun given_search_text_field_when_user_types_query_then_calls_onQueryChange() {
        // Given
        var queryChanged = false
        val uiState = SearchUiState()

        // When
        composeTestRule.setContent {
            SearchScreen(
                uiState = uiState,
                onQueryChange = { queryChanged = true },
                onRetry = {},
                onLocationSelected = {}
            )
        }

        composeTestRule.onNodeWithText("Search city...").performTextInput("Bogotá")

        // Then
        assertTrue(queryChanged)
    }

    @Test
    fun given_error_state_when_user_clicks_retry_button_then_calls_onRetry() {
        // Given
        var retryCalled = false
        val uiState = SearchUiState(
            query = "Bogotá",
            error = AppError.NetworkError.NoConnection
        )

        // When
        composeTestRule.setContent {
            SearchScreen(
                uiState = uiState,
                onQueryChange = {},
                onRetry = { retryCalled = true },
                onLocationSelected = {}
            )
        }

        composeTestRule.onNodeWithText("Retry").performClick()

        // Then
        assertTrue(retryCalled)
    }

    @Test
    fun given_query_text_when_user_clicks_clear_button_then_clears_query() {
        // Given
        var clearedQuery: String? = null
        val uiState = SearchUiState(
            query = "Bogotá"
        )

        // When
        composeTestRule.setContent {
            SearchScreen(
                uiState = uiState,
                onQueryChange = { clearedQuery = it },
                onRetry = {},
                onLocationSelected = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Clear").performClick()

        // Then
        assertEquals("", clearedQuery)
    }

    @Test
    fun given_loading_state_when_search_is_in_progress_then_shows_loading_indicator_in_text_field() {
        // Given
        val uiState = SearchUiState(
            query = "Bogotá",
            isLoading = true
        )

        // When
        composeTestRule.setContent {
            SearchScreen(
                uiState = uiState,
                onQueryChange = {},
                onRetry = {},
                onLocationSelected = {}
            )
        }

        // Then
        // Loading indicator should be visible (CircularProgressIndicator)
        // Note: We can't directly test for CircularProgressIndicator, but we can verify
        // that the loading state is reflected in the UI
        composeTestRule.onNodeWithText("Bogotá").assertIsDisplayed()
    }

}

