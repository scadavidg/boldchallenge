package com.boldchallenge.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boldchallenge.ui.components.GradientBackground
import com.boldchallenge.ui.components.GradientType
import com.boldchallenge.ui.theme.BoldchallengeTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    var startAnimation by remember { mutableStateOf(false) }
    
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "alpha"
    )
    
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.6f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    val offsetAnim by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 50f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "offset"
    )
    
    // Floating animation for the icon
    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
    )
    
    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2000)
        onNavigateToSearch()
    }
    
    GradientBackground(
        gradientType = GradientType.SUNNY,
        modifier = modifier
    ) {
        // Decorative circles in background
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-100).dp, y = (-200).dp)
                .alpha(0.1f)
                .background(Color.White, shape = androidx.compose.foundation.shape.CircleShape)
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = 120.dp, y = 250.dp)
                .alpha(0.1f)
                .background(Color.White, shape = androidx.compose.foundation.shape.CircleShape)
        )
        
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .alpha(alphaAnim)
                    .scale(scaleAnim)
                    .offset(y = offsetAnim.dp)
            ) {
            // Weather Icon with floating animation
            Text(
                text = "🌤️",
                fontSize = 100.sp,
                modifier = Modifier.offset(y = floatingOffset.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // App Name
            Text(
                text = "Weather",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Tagline
            Text(
                text = "Your weather, at a glance",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
        
            // Loading indicator at bottom
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 64.dp)
                    .alpha(alphaAnim)
            ) {
                LoadingDots()
            }
        }
    }
}

@Composable
private fun LoadingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(3) { index ->
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = index * 200),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_$index"
            )
            
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .alpha(alpha)
                    .background(
                        Color.White,
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
        }
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true, name = "Splash Screen")
@Composable
private fun SplashScreenPreview() {
    BoldchallengeTheme {
        SplashScreen(onNavigateToSearch = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, name = "Splash Screen - Dark")
@Composable
private fun SplashScreenDarkPreview() {
    BoldchallengeTheme(darkTheme = true) {
        SplashScreen(onNavigateToSearch = {})
    }
}

@Preview(showBackground = true, name = "Loading Dots")
@Composable
private fun LoadingDotsPreview() {
    BoldchallengeTheme {
        Box(
            modifier = Modifier
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            com.boldchallenge.ui.theme.GradientDayTop,
                            com.boldchallenge.ui.theme.GradientDayBottom
                        )
                    )
                )
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            LoadingDots()
        }
    }
}
