package com.example.smartwaste_user

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutElastic
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SmartWasteUserTheme {
                var showSplash by remember { mutableStateOf(true) }
                var showMainApp by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    delay(3500L)
                    showSplash = false

                    delay(400L)
                    showMainApp = true
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AnimatedVisibility(
                            visible = showMainApp,
                            enter = fadeIn(
                                animationSpec = tween(
                                    durationMillis = 800,
                                    easing = EaseInOutCubic
                                )
                            ) + slideInVertically(
                                animationSpec = tween(
                                    durationMillis = 800,
                                    easing = EaseInOutCubic
                                ),
                                initialOffsetY = { it / 6 }
                            ) + scaleIn(
                                animationSpec = tween(
                                    durationMillis = 800,
                                    easing = EaseInOutCubic
                                ),
                                initialScale = 0.92f
                            ),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val authViewModel: AuthViewModel = hiltViewModel()
                            val onboardingViewModel: OnboardingViewModel = hiltViewModel()

                            AppNavigation(
                                viewModel = authViewModel,
                                onboardingViewModel = onboardingViewModel,
                                shouldShowOnboarding = false,
                                currentUser = Firebase.auth.currentUser
                            )
                        }

                        AnimatedVisibility(
                            visible = showSplash,
                            exit = fadeOut(
                                animationSpec = tween(
                                    durationMillis = 600,
                                    easing = EaseInOutCubic
                                )
                            ) + slideOutVertically(
                                animationSpec = tween(
                                    durationMillis = 600,
                                    easing = EaseInOutCubic
                                ),
                                targetOffsetY = { -it / 6 }
                            ) + scaleOut(
                                animationSpec = tween(
                                    durationMillis = 600,
                                    easing = EaseInOutCubic
                                ),
                                targetScale = 1.05f
                            ),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            SplashScreen()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen() {
    val logoScale = remember { Animatable(0f) }
    val logoAlpha = remember { Animatable(0f) }
    val logoRotation = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val textScale = remember { Animatable(0.8f) }
    val backgroundAlpha = remember { Animatable(0f) }
    val subtitleAlpha = remember { Animatable(0f) }
    val coruutine = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        backgroundAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = EaseInOutCubic)
        )

        logoAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = EaseInOutCubic)
        )

        coruutine.launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        coruutine.launch {
            logoRotation.animateTo(
                targetValue = 360f,
                animationSpec = tween(
                    durationMillis = 1200,
                    easing = EaseOutElastic
                )
            )
        }

        delay(400L)
        coruutine.launch {
            textAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800, easing = EaseInOutCubic)
            )
        }

        coruutine.launch {
            textScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }

        delay(300L)
        subtitleAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = EaseInOutCubic)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1B5E20),
                        Color(0xFF2E7D32),
                        Color(0xFF388E3C)
                    )
                ),
                alpha = backgroundAlpha.value
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.White.copy(alpha = 0.1f),
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
                    modifier = Modifier
                        .size(80.dp)
                        .scale(1.1f)
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
                    .alpha(textAlpha.value)
                    .scale(textScale.value),
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Managing Waste Smartly",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.95f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(subtitleAlpha.value)
                    .scale(textScale.value),
                letterSpacing = 0.8.sp
            )
        }
    }
}