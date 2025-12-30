package com.boldchallenge.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.boldchallenge.ui.theme.BoldchallengeTheme

/**
 * An animated weather icon that loads from URL with a subtle breathing animation.
 * Use for displaying weather condition icons from the API.
 */
@Composable
fun AnimatedWeatherIcon(
    iconUrl: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "weather_icon")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(iconUrl)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            modifier = Modifier
                .size(size)
                .scale(scale),
            contentScale = ContentScale.Fit
        )
    }
}

/**
 * Converts weather condition text to an emoji representation.
 * Use as fallback when icon URL is not available or for decorative purposes.
 */
@Composable
fun WeatherEmoji(
    condition: String,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp
) {
    val emoji = getWeatherEmoji(condition)
    
    Text(
        text = emoji,
        fontSize = (size.value * 0.8f).sp,
        modifier = modifier
    )
}

/**
 * Utility function to get emoji for weather condition.
 */
fun getWeatherEmoji(condition: String): String = when {
    condition.contains("sunny", ignoreCase = true) || 
    condition.contains("clear", ignoreCase = true) -> "☀️"
    condition.contains("partly cloudy", ignoreCase = true) -> "⛅"
    condition.contains("cloudy", ignoreCase = true) || 
    condition.contains("overcast", ignoreCase = true) -> "☁️"
    condition.contains("rain", ignoreCase = true) || 
    condition.contains("drizzle", ignoreCase = true) -> "🌧️"
    condition.contains("thunder", ignoreCase = true) || 
    condition.contains("storm", ignoreCase = true) -> "⛈️"
    condition.contains("snow", ignoreCase = true) -> "❄️"
    condition.contains("fog", ignoreCase = true) || 
    condition.contains("mist", ignoreCase = true) -> "🌫️"
    condition.contains("wind", ignoreCase = true) -> "💨"
    else -> "🌤️"
}

@Preview(showBackground = true, name = "Animated Weather Icon")
@Composable
private fun AnimatedWeatherIconPreview() {
    BoldchallengeTheme {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF4FACFE), Color(0xFF00F2FE))
                    )
                )
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedWeatherIcon(
                    iconUrl = "https://cdn.weatherapi.com/weather/64x64/day/116.png",
                    contentDescription = "Partly cloudy",
                    size = 100.dp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Partly Cloudy",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Weather Emoji - Sunny")
@Composable
private fun WeatherEmojiSunnyPreview() {
    BoldchallengeTheme {
        Box(
            modifier = Modifier
                .background(Color(0xFF4FACFE))
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                WeatherEmoji(
                    condition = "Sunny",
                    size = 80.dp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sunny",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Weather Emoji - All Conditions")
@Composable
private fun WeatherEmojiAllPreview() {
    val conditions = listOf(
        "Sunny", "Partly cloudy", "Cloudy", "Rain",
        "Thunderstorm", "Snow", "Fog", "Windy"
    )
    
    BoldchallengeTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF4FACFE), Color(0xFF00F2FE))
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                conditions.chunked(4).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxSize().weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { condition ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                WeatherEmoji(
                                    condition = condition,
                                    size = 48.dp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = condition,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Weather Icons Sizes")
@Composable
private fun WeatherIconSizesPreview() {
    BoldchallengeTheme {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0F2027), Color(0xFF2C5364))
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    WeatherEmoji(condition = "Sunny", size = 32.dp)
                    Text("32dp", color = Color.White, style = MaterialTheme.typography.labelSmall)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    WeatherEmoji(condition = "Sunny", size = 48.dp)
                    Text("48dp", color = Color.White, style = MaterialTheme.typography.labelSmall)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    WeatherEmoji(condition = "Sunny", size = 64.dp)
                    Text("64dp", color = Color.White, style = MaterialTheme.typography.labelSmall)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    WeatherEmoji(condition = "Sunny", size = 80.dp)
                    Text("80dp", color = Color.White, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

