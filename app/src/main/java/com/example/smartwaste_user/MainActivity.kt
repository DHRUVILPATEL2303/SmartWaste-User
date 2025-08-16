package com.example.smartwaste_user

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartwaste_user.presentation.navigation.AppNavigation
import com.example.smartwaste_user.presentation.viewmodels.AuthViewModel
import com.example.smartwaste_user.presentation.viewmodels.OnboardingViewModel
import com.example.smartwaste_user.ui.theme.SmartWasteUserTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SmartWasteUserTheme {
                var showCustomSplash by remember { mutableStateOf(true) }

                val mainAppContent: @Composable () -> Unit = {
                    val authViewModel: AuthViewModel = hiltViewModel()
                    val onboardingViewModel: OnboardingViewModel = hiltViewModel()

                    AppNavigation(
                        viewModel = authViewModel,
                        onboardingViewModel = onboardingViewModel,
                        shouldShowOnboarding = false,
                        currentUser = Firebase.auth.currentUser
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AnimatedVisibility(
                            visible = !showCustomSplash,
                            enter = fadeIn(animationSpec = tween(durationMillis = 800, delayMillis = 200)),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            mainAppContent()
                        }

                        AnimatedVisibility(
                            visible = showCustomSplash,
                            exit = fadeOut(animationSpec = tween(durationMillis = 600, easing = EaseInOutCubic)),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            SplashScreen(onFinished = { showCustomSplash = false })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    // Animation states
    val backgroundAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.5f) }
    val logoAlpha = remember { Animatable(0f) }
    val textYOffset = remember { Animatable(100f) }
    val textAlpha = remember { Animatable(0f) }
    val subtitleAlpha = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "halo_pulse")
    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        ), label = "halo_alpha"
    )

    LaunchedEffect(Unit) {
        backgroundAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500)
        )

        launch {
            logoAlpha.animateTo(1f, tween(durationMillis = 800, delayMillis = 200))
        }
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        delay(600L)
        launch {
            textYOffset.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
        launch {
            textAlpha.animateTo(1f, tween(durationMillis = 700))
        }

        delay(300L)
        subtitleAlpha.animateTo(1f, tween(durationMillis = 600))

        delay(1500L)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1B5E20),
                        Color(0xFF2E7D32),
                        Color(0xFF4CAF50)
                    )
                )
            )
            .alpha(backgroundAlpha.value),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .graphicsLayer {
                        scaleX = logoScale.value
                        scaleY = logoScale.value
                        alpha = logoAlpha.value
                    }
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = haloAlpha),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Recycling,
                    contentDescription = "Smart Waste Logo",
                    tint = Color.White,
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "SmartWaste",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .offset { IntOffset(0, textYOffset.value.roundToInt()) }
                    .alpha(textAlpha.value),
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Managing Waste Smartly",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(subtitleAlpha.value),
                letterSpacing = 0.8.sp
            )
        }
    }
}