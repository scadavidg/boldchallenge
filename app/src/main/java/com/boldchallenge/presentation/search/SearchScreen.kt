package com.boldchallenge.presentation.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import android.content.res.Configuration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boldchallenge.ui.components.GlassCard
import com.boldchallenge.ui.components.GradientBackground
import com.boldchallenge.ui.components.GradientType
import com.boldchallenge.ui.theme.BoldchallengeTheme
import com.boldchallenge.ui.theme.GradientDayBottom
import com.boldchallenge.ui.theme.GradientDayTop
import com.boldchallenge.ui.theme.NightBlue
import com.boldchallenge.ui.theme.SkyBlue
import com.boldchallenge.ui.theme.StormGray
import com.domain.model.Location

/**
 * Search Screen (Pure UI)
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
fun SearchScreen(
    uiState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onRetry: () -> Unit,
    onLocationSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    GradientBackground(
        gradientType = GradientType.SUNNY,
        modifier = modifier
    ) {
        // Decorative elements
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = 200.dp, y = (-50).dp)
                .alpha(0.1f)
                .background(Color.White, shape = CircleShape)
        )

        if (isLandscape) {
            // Landscape: Side-by-side layout
            SearchScreenLandscape(
                uiState = uiState,
                onQueryChange = onQueryChange,
                onClear = { onQueryChange("") },
                onSearch = { focusManager.clearFocus() },
                onRetry = onRetry,
                onLocationSelected = onLocationSelected
            )
        } else {
            // Portrait: Vertical layout
            SearchScreenPortrait(
                uiState = uiState,
                onQueryChange = onQueryChange,
                onClear = { onQueryChange("") },
                onSearch = { focusManager.clearFocus() },
                onRetry = onRetry,
                onLocationSelected = onLocationSelected
            )
        }
    }
}

@Composable
private fun SearchScreenPortrait(
    uiState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onSearch: () -> Unit,
    onRetry: () -> Unit,
    onLocationSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Header
        Text(
            text = "Explore",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Text(
            text = "Search any city in the world",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        SearchTextField(
            query = uiState.query,
            onQueryChange = onQueryChange,
            onClear = onClear,
            isLoading = uiState.isLoading,
            onSearch = onSearch
        )

        Spacer(modifier = Modifier.height(24.dp))

        SearchContent(
            uiState = uiState,
            onRetry = onRetry,
            onLocationSelected = onLocationSelected,
            isLandscape = false,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SearchScreenLandscape(
    uiState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onSearch: () -> Unit,
    onRetry: () -> Unit,
    onLocationSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Left side: Header and Search
        Column(
            modifier = Modifier
                .weight(0.35f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Explore",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Search any city in the world",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            SearchTextField(
                query = uiState.query,
                onQueryChange = onQueryChange,
                onClear = onClear,
                isLoading = uiState.isLoading,
                onSearch = onSearch
            )
        }

        // Right side: Results
        Box(
            modifier = Modifier
                .weight(0.65f)
                .fillMaxHeight()
        ) {
            SearchContent(
                uiState = uiState,
                onRetry = onRetry,
                onLocationSelected = onLocationSelected,
                isLandscape = true
            )
        }
    }
}

@Composable
private fun SearchContent(
    uiState: SearchUiState,
    onRetry: () -> Unit,
    onLocationSelected: (String) -> Unit,
    isLandscape: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading && uiState.locations.isEmpty() -> {
                LoadingContent()
            }
            uiState.showError -> {
                ErrorContent(onRetry = onRetry)
            }
            uiState.showQueryTooShort -> {
                QueryTooShortContent()
            }
            uiState.showEmptyState -> {
                EmptyContent()
            }
            uiState.locations.isNotEmpty() -> {
                LocationList(
                    locations = uiState.locations,
                    isLoading = uiState.isLoading,
                    onLocationClick = { location -> onLocationSelected(location.name) },
                    isLandscape = isLandscape
                )
            }
            else -> {
                InitialContent()
            }
        }
    }
}

@Composable
private fun SearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    isLoading: Boolean,
    onSearch: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = StormGray,
                modifier = Modifier.size(24.dp)
            )

            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        "Search city...",
                        color = StormGray.copy(alpha = 0.6f)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = SkyBlue
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() })
            )

            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = SkyBlue
                )
            }

            AnimatedVisibility(
                visible = query.isNotEmpty() && !isLoading,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = StormGray
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationList(
    locations: List<Location>,
    isLoading: Boolean,
    onLocationClick: (Location) -> Unit,
    isLandscape: Boolean = false
) {
    if (isLandscape) {
        // Grid layout for landscape - show more items at once
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            itemsIndexed(
                items = locations,
                key = { index, location -> location.id ?: "${location.name}_${location.country}_$index" }
            ) { index, location ->
                LocationCardCompact(
                    location = location,
                    onClick = { onLocationClick(location) },
                    animationDelay = index * 30
                )
            }
        }
    } else {
        // List layout for portrait
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            itemsIndexed(
                items = locations,
                key = { index, location -> location.id ?: "${location.name}_${location.country}_$index" }
            ) { index, location ->
                LocationCard(
                    location = location,
                    onClick = { onLocationClick(location) },
                    animationDelay = index * 50
                )
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }
    }
}

/** Compact location card for landscape grid */
@Composable
private fun LocationCardCompact(
    location: Location,
    onClick: () -> Unit,
    animationDelay: Int
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animationDelay.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.95f)
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            cornerRadius = 16.dp,
            backgroundColor = Color.White.copy(alpha = 0.9f),
            borderColor = Color.White.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = location.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = NightBlue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (location.country.isNotEmpty()) {
                        Text(
                            text = location.country,
                            style = MaterialTheme.typography.bodySmall,
                            color = StormGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Text(
                    text = "→",
                    color = SkyBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun LocationCard(
    location: Location,
    onClick: () -> Unit,
    animationDelay: Int
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animationDelay.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 2 }
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = Color.Black.copy(alpha = 0.08f)
                )
                .clickable(onClick = onClick),
            cornerRadius = 20.dp,
            backgroundColor = Color.White.copy(alpha = 0.9f),
            borderColor = Color.White.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = location.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = NightBlue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (location.region != null || location.country.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = listOfNotNull(location.region, location.country)
                                .joinToString(", "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = StormGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Arrow indicator
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = SkyBlue.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "→",
                        color = SkyBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = Color.White,
            strokeWidth = 3.dp,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Searching...",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun ErrorContent(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "😕",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "We couldn't complete the search",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = SkyBlue
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "Retry",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun QueryTooShortContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⌨️",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Keep typing",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Type at least 2 characters\nto start searching",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🔍",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No results",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Try another city name",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun InitialContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🌍",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Discover the weather",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Type at least 2 characters\nto start searching",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true, name = "Search - Initial State")
@Composable
private fun SearchScreenInitialPreview() {
    BoldchallengeTheme {
        SearchScreen(
            uiState = SearchUiState(),
            onQueryChange = {},
            onRetry = {},
            onLocationSelected = {}
        )
    }
}

@Preview(showBackground = true, name = "Search - Loading State")
@Composable
private fun SearchScreenLoadingPreview() {
    BoldchallengeTheme {
        SearchScreen(
            uiState = SearchUiState(
                query = "Bogotá",
                isLoading = true
            ),
            onQueryChange = {},
            onRetry = {},
            onLocationSelected = {}
        )
    }
}

@Preview(showBackground = true, name = "Search - Results State")
@Composable
private fun SearchScreenResultsPreview() {
    BoldchallengeTheme {
        SearchScreen(
            uiState = SearchUiState(
                query = "Bogotá",
                locations = listOf(
                    Location(
                        id = 1,
                        name = "Bogotá",
                        region = "Cundinamarca",
                        country = "Colombia",
                        lat = 4.7110,
                        lon = -74.0721,
                        url = null
                    ),
                    Location(
                        id = 2,
                        name = "Medellín",
                        region = "Antioquia",
                        country = "Colombia",
                        lat = 6.2442,
                        lon = -75.5812,
                        url = null
                    ),
                    Location(
                        id = 3,
                        name = "Cali",
                        region = "Valle del Cauca",
                        country = "Colombia",
                        lat = 3.4516,
                        lon = -76.5320,
                        url = null
                    )
                )
            ),
            onQueryChange = {},
            onRetry = {},
            onLocationSelected = {}
        )
    }
}

@Preview(showBackground = true, name = "Search - Query Too Short")
@Composable
private fun SearchScreenQueryTooShortPreview() {
    BoldchallengeTheme {
        SearchScreen(
            uiState = SearchUiState(
                query = "N"
            ),
            onQueryChange = {},
            onRetry = {},
            onLocationSelected = {}
        )
    }
}

@Preview(showBackground = true, name = "Search - Empty State")
@Composable
private fun SearchScreenEmptyPreview() {
    BoldchallengeTheme {
        SearchScreen(
            uiState = SearchUiState(
                query = "xyz123",
                isEmpty = true
            ),
            onQueryChange = {},
            onRetry = {},
            onLocationSelected = {}
        )
    }
}

@Preview(showBackground = true, name = "Search - Error State")
@Composable
private fun SearchScreenErrorPreview() {
    BoldchallengeTheme {
        SearchScreen(
            uiState = SearchUiState(
                query = "Bogotá",
                error = com.domain.error.AppError.NetworkError.NoConnection
            ),
            onQueryChange = {},
            onRetry = {},
            onLocationSelected = {}
        )
    }
}

@Preview(showBackground = true, name = "Location Card")
@Composable
private fun LocationCardPreview() {
    BoldchallengeTheme {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(GradientDayTop, GradientDayBottom)
                    )
                )
                .padding(16.dp)
        ) {
            LocationCard(
                location = Location(
                    id = 1,
                    name = "Bogotá",
                    region = "Cundinamarca",
                    country = "Colombia",
                    lat = 4.7110,
                    lon = -74.0721,
                    url = null
                ),
                onClick = {},
                animationDelay = 0
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, name = "Search - Dark Mode")
@Composable
private fun SearchScreenDarkPreview() {
    BoldchallengeTheme(darkTheme = true) {
        SearchScreen(
            uiState = SearchUiState(
                query = "London",
                locations = listOf(
                    Location(
                        id = 1,
                        name = "London",
                        region = "City of London",
                        country = "United Kingdom",
                        lat = 51.5074,
                        lon = -0.1278,
                        url = null
                    )
                )
            ),
            onQueryChange = {},
            onRetry = {},
            onLocationSelected = {}
        )
    }
}

@Preview(
    showBackground = true,
    name = "Search - Landscape",
    device = "spec:width=891dp,height=411dp,orientation=landscape"
)
@Composable
private fun SearchScreenLandscapePreview() {
    BoldchallengeTheme {
        SearchScreen(
            uiState = SearchUiState(
                query = "New",
                locations = listOf(
                    Location(id = 1, name = "New York", region = "New York", country = "USA", lat = 40.7, lon = -74.0, url = null),
                    Location(id = 2, name = "New Delhi", region = "Delhi", country = "India", lat = 28.6, lon = 77.2, url = null),
                    Location(id = 3, name = "Newcastle", region = "England", country = "UK", lat = 54.9, lon = -1.6, url = null),
                    Location(id = 4, name = "New Orleans", region = "Louisiana", country = "USA", lat = 29.9, lon = -90.0, url = null)
                )
            ),
            onQueryChange = {},
            onRetry = {},
            onLocationSelected = {}
        )
    }
}
