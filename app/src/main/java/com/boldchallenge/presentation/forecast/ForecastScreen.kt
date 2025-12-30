package com.boldchallenge.presentation.forecast

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalInspectionMode
import android.content.res.Configuration
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.boldchallenge.ui.components.AnimatedWeatherIcon
import com.boldchallenge.ui.components.GradientBackground
import com.boldchallenge.ui.components.GradientType
import com.boldchallenge.ui.components.WeatherEmoji
import com.boldchallenge.ui.theme.BoldchallengeTheme
import com.boldchallenge.ui.theme.GradientDayBottom
import com.boldchallenge.ui.theme.GradientDayTop
import com.boldchallenge.ui.theme.NightBlue
import com.boldchallenge.ui.theme.SkyBlue
import com.boldchallenge.ui.theme.StormGray
import com.domain.model.Forecast
import com.domain.model.ForecastDay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Forecast Screen (Pure UI)
 * 
 * Responsibilities:
 * - Render UI based on UiState
 * - Emit events via callbacks
 * 
 * Does NOT have:
 * - ViewModel
 * - Hilt
 * - Flow observation
 * - Business logic
 */
@Composable
fun ForecastScreen(
    uiState: ForecastUiState,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    GradientBackground(
        gradientType = GradientType.SUNNY,
        modifier = modifier
    ) {
        // Decorative elements
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-80).dp, y = (-100).dp)
                .alpha(0.08f)
                .background(Color.White, shape = CircleShape)
        )
        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = 250.dp, y = 400.dp)
                .alpha(0.06f)
                .background(Color.White, shape = CircleShape)
        )

        when {
            uiState.showLoading -> {
                LoadingContent()
            }

            uiState.showError -> {
                ErrorContent(onRetry = onRetry)
            }

            uiState.forecast != null -> {
                ForecastContent(
                    forecast = uiState.forecast,
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = onRefresh,
                    onNavigateBack = onNavigateBack
                )
            }
        }
    }
}

@Composable
private fun ForecastContent(
    forecast: Forecast,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isInPreview = LocalInspectionMode.current
    var isVisible by remember { mutableStateOf(isInPreview) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    if (isLandscape) {
        ForecastContentLandscape(
            forecast = forecast,
            isRefreshing = isRefreshing,
            isVisible = isVisible,
            onRefresh = onRefresh,
            onNavigateBack = onNavigateBack
        )
    } else {
        ForecastContentPortrait(
            forecast = forecast,
            isRefreshing = isRefreshing,
            isVisible = isVisible,
            onRefresh = onRefresh,
            onNavigateBack = onNavigateBack
        )
    }
}

@Composable
private fun ForecastContentPortrait(
    forecast: Forecast,
    isRefreshing: Boolean,
    isVisible: Boolean,
    onRefresh: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(20.dp)
    ) {
        ForecastHeader(
            locationName = forecast.locationName,
            isRefreshing = isRefreshing,
            isVisible = isVisible,
            onRefresh = onRefresh,
            onNavigateBack = onNavigateBack
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Today's highlight
        forecast.days.firstOrNull()?.let { today ->
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600, 100)) + scaleIn(tween(600, 100), initialScale = 0.9f)
            ) {
                TodayCard(forecastDay = today)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Upcoming days
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(600, 200)) + slideInVertically(tween(600, 200)) { it / 4 }
        ) {
            UpcomingDaysSection(forecast = forecast)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ForecastContentLandscape(
    forecast: Forecast,
    isRefreshing: Boolean,
    isVisible: Boolean,
    onRefresh: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Left side: Header with integrated today card
        Column(
            modifier = Modifier
                .weight(0.45f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            forecast.days.firstOrNull()?.let { today ->
                ForecastHeaderWithToday(
                    locationName = forecast.locationName,
                    today = today,
                    isRefreshing = isRefreshing,
                    isVisible = isVisible,
                    onRefresh = onRefresh,
                    onNavigateBack = onNavigateBack
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Right side: All days in a column (no scroll needed)
        Column(
            modifier = Modifier
                .weight(0.55f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(400, 100)) + slideInHorizontally(tween(400, 100)) { it / 3 }
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "3-Day Forecast",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    forecast.days.drop(1).forEachIndexed { index, day ->
                        ForecastDayRowCompact(
                            forecastDay = day,
                            isToday = false,
                            animationDelay = index * 80
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ForecastHeader(
    locationName: String,
    isRefreshing: Boolean,
    isVisible: Boolean,
    onRefresh: () -> Unit,
    onNavigateBack: () -> Unit,
    compact: Boolean = false
) {
    Column {
        // Back button
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(300))
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(if (compact) 40.dp else 44.dp)
                    .background(
                        Color.White.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(if (compact) 12.dp else 16.dp))

        // Location and refresh
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -it / 2 }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = locationName,
                        style = if (compact) MaterialTheme.typography.headlineSmall 
                               else MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (!compact) {
                        Text(
                            text = "3-day forecast",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onRefresh,
                    enabled = !isRefreshing,
                    modifier = Modifier
                        .size(if (compact) 40.dp else 44.dp)
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                        .semantics { contentDescription = "Refresh" }
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

/** Header with integrated today card for landscape mode */
@Composable
private fun ForecastHeaderWithToday(
    locationName: String,
    today: ForecastDay,
    isRefreshing: Boolean,
    isVisible: Boolean,
    onRefresh: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top row: Back button, Location, Refresh
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Color.White.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = locationName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            IconButton(
                onClick = onRefresh,
                enabled = !isRefreshing,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Color.White.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
                    .semantics { contentDescription = "Refresh" }
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.White
                    )
                }
            }
        }

        // Today's card - square-ish design
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(400)) + scaleIn(tween(400), initialScale = 0.95f)
        ) {
            TodayCardSquare(forecastDay = today)
        }
    }
}

@Composable
private fun UpcomingDaysSection(forecast: Forecast) {
    Column {
        Text(
            text = "Upcoming days",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        forecast.days.drop(1).forEachIndexed { index, day ->
            ForecastDayRow(
                forecastDay = day,
                animationDelay = 300 + (index * 100)
            )
            if (index < forecast.days.size - 2) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

/** Square-ish Today card for landscape header */
@Composable
private fun TodayCardSquare(forecastDay: ForecastDay) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.2f) // Make it more square (slightly wider than tall)
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color.Black.copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        Color.White.copy(alpha = 0.90f)
                    )
                )
            )
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Date label
            Text(
                text = "Today",
                style = MaterialTheme.typography.titleMedium,
                color = SkyBlue,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = formatDateShort(forecastDay.date),
                style = MaterialTheme.typography.bodySmall,
                color = StormGray,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Weather icon
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "float")
                val offset by infiniteTransition.animateFloat(
                    initialValue = -4f,
                    targetValue = 4f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(3000, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "offset"
                )

                AnimatedWeatherIcon(
                    iconUrl = forecastDay.conditionIconUrl,
                    contentDescription = forecastDay.conditionText,
                    modifier = Modifier.offset(y = offset.dp),
                    size = 70.dp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Temperature - large and prominent
            Text(
                text = "${forecastDay.avgTempC.toInt()}°",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = NightBlue
            )

            // Condition text
            Text(
                text = forecastDay.conditionText,
                style = MaterialTheme.typography.bodyMedium,
                color = StormGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

/** Compact Today card for landscape mode */
@Composable
private fun TodayCardCompact(forecastDay: ForecastDay) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        Color.White.copy(alpha = 0.85f)
                    )
                )
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Date and condition
            Column {
                Text(
                    text = "Today",
                    style = MaterialTheme.typography.titleSmall,
                    color = SkyBlue,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = forecastDay.conditionText,
                    style = MaterialTheme.typography.bodySmall,
                    color = StormGray,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Center: Icon
            AnimatedWeatherIcon(
                iconUrl = forecastDay.conditionIconUrl,
                contentDescription = forecastDay.conditionText,
                size = 60.dp
            )

            // Right: Temperature
            Text(
                text = "${forecastDay.avgTempC.toInt()}°",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = NightBlue
            )
        }
    }
}

/** Compact day row for landscape mode */
@Composable
private fun ForecastDayRowCompact(
    forecastDay: ForecastDay,
    isToday: Boolean,
    animationDelay: Int
) {
    val isInPreview = LocalInspectionMode.current
    var isVisible by remember { mutableStateOf(isInPreview) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animationDelay.toLong())
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)) + slideInHorizontally(tween(200)) { it / 4 }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (isToday) Color.White.copy(alpha = 0.25f) 
                    else Color.White.copy(alpha = 0.12f)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Day name
                Text(
                    text = if (isToday) "Today" else formatDayName(forecastDay.date),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier.width(80.dp)
                )

                // Icon and condition
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    AnimatedWeatherIcon(
                        iconUrl = forecastDay.conditionIconUrl,
                        contentDescription = forecastDay.conditionText,
                        size = 36.dp
                    )
                    Text(
                        text = forecastDay.conditionText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Temperature
                Text(
                    text = "${forecastDay.avgTempC.toInt()}°",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun TodayCard(forecastDay: ForecastDay) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color.Black.copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(28.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        Color.White.copy(alpha = 0.85f)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Date label
            Text(
                text = "Today",
                style = MaterialTheme.typography.titleMedium,
                color = SkyBlue,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = formatDateFull(forecastDay.date),
                style = MaterialTheme.typography.bodyMedium,
                color = StormGray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Weather icon with animation
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "float")
                val offset by infiniteTransition.animateFloat(
                    initialValue = -6f,
                    targetValue = 6f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(3000, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "offset"
                )

                AnimatedWeatherIcon(
                    iconUrl = forecastDay.conditionIconUrl,
                    contentDescription = forecastDay.conditionText,
                    modifier = Modifier.offset(y = offset.dp),
                    size = 100.dp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Temperature
            Text(
                text = "${forecastDay.avgTempC.toInt()}°",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = NightBlue
            )

            // Condition
            Text(
                text = forecastDay.conditionText,
                style = MaterialTheme.typography.titleMedium,
                color = StormGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun ForecastDayRow(
    forecastDay: ForecastDay,
    animationDelay: Int
) {
    val isInPreview = LocalInspectionMode.current
    var isVisible by remember { mutableStateOf(isInPreview) } // Start visible in previews

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animationDelay.toLong())
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 3 }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.15f))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formatDayName(forecastDay.date),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = formatDateShort(forecastDay.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                // Condition and icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = forecastDay.conditionText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1,
                        modifier = Modifier.width(80.dp),
                        textAlign = TextAlign.End
                    )

                    AnimatedWeatherIcon(
                        iconUrl = forecastDay.conditionIconUrl,
                        contentDescription = forecastDay.conditionText,
                        size = 48.dp
                    )
                }

                // Temperature
                Text(
                    text = "${forecastDay.avgTempC.toInt()}°",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.width(50.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated loading indicator
            val infiniteTransition = rememberInfiniteTransition(label = "loading")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing)
                ),
                label = "rotation"
            )

            WeatherEmoji(
                condition = "Sunny",
                size = 64.dp,
                modifier = Modifier.offset(y = kotlin.math.sin(rotation * 0.05).dp * 10)
            )

            Spacer(modifier = Modifier.height(24.dp))

            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Loading forecast...",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun ErrorContent(onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WeatherEmoji(
                condition = "Thunderstorm",
                size = 80.dp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Couldn't load forecast",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Check your connection and try again",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = SkyBlue
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(52.dp)
            ) {
                Text(
                    "Retry",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    }
}

// Date formatting helpers
private fun formatDateFull(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
        val dayOfMonth = date.dayOfMonth
        val month = date.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        "$month $dayOfMonth"
    } catch (e: Exception) {
        dateString
    }
}

private fun formatDayName(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
        date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
            .replaceFirstChar { it.uppercase() }
    } catch (e: Exception) {
        dateString
    }
}

private fun formatDateShort(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
        val dayOfMonth = date.dayOfMonth
        val month = date.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
        "$month $dayOfMonth"
    } catch (e: Exception) {
        dateString
    }
}

// ==================== PREVIEWS ====================

private val sampleForecast = Forecast(
    locationName = "London",
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

@Preview(showBackground = true, name = "Forecast - Content")
@Composable
private fun ForecastScreenContentPreview() {
    BoldchallengeTheme {
        ForecastScreen(
            uiState = ForecastUiState(
                forecast = sampleForecast,
                isLoading = false
            ),
            onRetry = {},
            onRefresh = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Forecast - Loading")
@Composable
private fun ForecastScreenLoadingPreview() {
    BoldchallengeTheme {
        ForecastScreen(
            uiState = ForecastUiState(isLoading = true),
            onRetry = {},
            onRefresh = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Forecast - Error")
@Composable
private fun ForecastScreenErrorPreview() {
    BoldchallengeTheme {
        ForecastScreen(
            uiState = ForecastUiState(
                error = com.domain.error.AppError.NetworkError.NoConnection
            ),
            onRetry = {},
            onRefresh = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Forecast - Refreshing")
@Composable
private fun ForecastScreenRefreshingPreview() {
    BoldchallengeTheme {
        ForecastScreen(
            uiState = ForecastUiState(
                forecast = sampleForecast,
                isRefreshing = true
            ),
            onRetry = {},
            onRefresh = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, name = "Forecast - Dark Mode")
@Composable
private fun ForecastScreenDarkPreview() {
    BoldchallengeTheme(darkTheme = true) {
        ForecastScreen(
            uiState = ForecastUiState(
                forecast = sampleForecast
            ),
            onRetry = {},
            onRefresh = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Today Card")
@Composable
private fun TodayCardPreview() {
    BoldchallengeTheme {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(GradientDayTop, GradientDayBottom)
                    )
                )
                .padding(20.dp)
        ) {
            TodayCard(
                forecastDay = ForecastDay(
                    date = "2024-01-15",
                    avgTempC = 18.5,
                    conditionText = "Partly cloudy",
                    conditionIconUrl = "https://cdn.weatherapi.com/weather/64x64/day/116.png"
                )
            )
        }
    }
}

@Preview(showBackground = true, name = "Loading Content")
@Composable
private fun LoadingContentPreview() {
    BoldchallengeTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(GradientDayTop, GradientDayBottom)
                    )
                )
        ) {
            LoadingContent()
        }
    }
}

@Preview(showBackground = true, name = "Error Content")
@Composable
private fun ErrorContentPreview() {
    BoldchallengeTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(GradientDayTop, GradientDayBottom)
                    )
                )
        ) {
            ErrorContent(onRetry = {})
        }
    }
}

@Preview(
    showBackground = true,
    name = "Forecast - Landscape",
    device = "spec:width=891dp,height=411dp,orientation=landscape"
)
@Composable
private fun ForecastScreenLandscapePreview() {
    BoldchallengeTheme {
        ForecastScreen(
            uiState = ForecastUiState(
                forecast = sampleForecast,
                isLoading = false
            ),
            onRetry = {},
            onRefresh = {},
            onNavigateBack = {}
        )
    }
}

