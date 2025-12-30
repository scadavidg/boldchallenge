package com.boldchallenge.presentation.splash

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SplashScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun given_splash_screen_when_rendered_then_shows_app_name_and_tagline() {
            // Given
            var navigationCalled = false

            // When
            composeTestRule.setContent {
                SplashScreen(
                    onNavigateToSearch = { navigationCalled = true }
                )
            }

            // Then
            composeTestRule.onNodeWithText("Weather").assertIsDisplayed()
            composeTestRule.onNodeWithText("Your weather, at a glance").assertIsDisplayed()
        }
}

