package com.boldchallenge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.boldchallenge.ui.theme.BoldchallengeTheme

/**
 * Weather condition types for dynamic gradient backgrounds.
 */
enum class GradientType {
    SUNNY,
    CLOUDY,
    NIGHT,
    SUNSET,
    STORM
}

/**
 * A full-screen gradient background that changes based on weather conditions.
 * Use to create immersive weather-themed screens.
 */
@Composable
fun GradientBackground(
    gradientType: GradientType = GradientType.SUNNY,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val gradientColors = when (gradientType) {
        GradientType.SUNNY -> listOf(
            Color(0xFF4FACFE),
            Color(0xFF00F2FE)
        )
        GradientType.CLOUDY -> listOf(
            Color(0xFF8EC5FC),
            Color(0xFFE0C3FC)
        )
        GradientType.NIGHT -> listOf(
            Color(0xFF0F2027),
            Color(0xFF203A43),
            Color(0xFF2C5364)
        )
        GradientType.SUNSET -> listOf(
            Color(0xFFFA709A),
            Color(0xFFFEE140)
        )
        GradientType.STORM -> listOf(
            Color(0xFF373B44),
            Color(0xFF4286f4)
        )
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(colors = gradientColors)
            )
    ) {
        content()
    }
}

@Preview(showBackground = true, name = "Sunny Gradient")
@Composable
private fun GradientBackgroundSunnyPreview() {
    BoldchallengeTheme {
        GradientBackground(gradientType = GradientType.SUNNY) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "☀️ Sunny",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Cloudy Gradient")
@Composable
private fun GradientBackgroundCloudyPreview() {
    BoldchallengeTheme {
        GradientBackground(gradientType = GradientType.CLOUDY) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "☁️ Cloudy",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Night Gradient")
@Composable
private fun GradientBackgroundNightPreview() {
    BoldchallengeTheme {
        GradientBackground(gradientType = GradientType.NIGHT) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🌙 Night",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Sunset Gradient")
@Composable
private fun GradientBackgroundSunsetPreview() {
    BoldchallengeTheme {
        GradientBackground(gradientType = GradientType.SUNSET) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🌅 Sunset",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Storm Gradient")
@Composable
private fun GradientBackgroundStormPreview() {
    BoldchallengeTheme {
        GradientBackground(gradientType = GradientType.STORM) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⛈️ Storm",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "All Gradients")
@Composable
private fun AllGradientsPreview() {
    BoldchallengeTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            GradientType.entries.forEach { type ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = when (type) {
                                    GradientType.SUNNY -> listOf(Color(0xFF4FACFE), Color(0xFF00F2FE))
                                    GradientType.CLOUDY -> listOf(Color(0xFF8EC5FC), Color(0xFFE0C3FC))
                                    GradientType.NIGHT -> listOf(Color(0xFF0F2027), Color(0xFF2C5364))
                                    GradientType.SUNSET -> listOf(Color(0xFFFA709A), Color(0xFFFEE140))
                                    GradientType.STORM -> listOf(Color(0xFF373B44), Color(0xFF4286f4))
                                }
                            )
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = type.name,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

