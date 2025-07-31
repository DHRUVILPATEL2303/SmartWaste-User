package com.example.smartwaste_user.presentation.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.smartwaste_user.presentation.screens.Auth.LoginScreenUI
import com.example.smartwaste_user.presentation.screens.OnBoarding.OnBoardingScreenUI
import com.example.smartwaste_user.presentation.screens.Auth.SignUpScreenUI
import com.example.smartwaste_user.presentation.viewmodels.AuthViewModel
import com.example.smartwaste_user.presentation.viewmodels.OnboardingViewModel

@Composable
fun AppNavigation(
    viewModel: AuthViewModel = hiltViewModel<AuthViewModel>(),
    onboardingViewModel: OnboardingViewModel = hiltViewModel<OnboardingViewModel>(),
    shouldShowOnboarding: Boolean
) {
    val navController = rememberNavController()
    val isOnboardingCompleted by onboardingViewModel.onboardingCompleted.collectAsState(initial = false)

    val startDestination = if (shouldShowOnboarding) {
        SubNavigation.OnBoardingScreen
    } else {
        SubNavigation.AuthRoutes
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = 200,
                    easing = LinearEasing
                )
            ) + scaleIn(
                initialScale = 0.92f,
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                )
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it / 2 },
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                )
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = 200,
                    easing = LinearEasing
                )
            ) + scaleOut(
                targetScale = 0.92f,
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                )
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = 200,
                    easing = LinearEasing
                )
            ) + scaleIn(
                initialScale = 0.92f,
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                )
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                )
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = 200,
                    easing = LinearEasing
                )
            ) + scaleOut(
                targetScale = 0.92f,
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                )
            )
        }
    ) {
        navigation<SubNavigation.AuthRoutes>(
            startDestination = Routes.LoginScreen
        ) {
            composable<Routes.SignUpScreen> {
                SignUpScreenUI(navController = navController)
            }
            composable<Routes.LoginScreen> {
                LoginScreenUI(navController = navController)
            }
        }

        navigation<SubNavigation.OnBoardingScreen>(
            startDestination = Routes.OnBoardingScreen
        ) {
            composable<Routes.OnBoardingScreen> {
                OnBoardingScreenUI(navController = navController)
            }
        }

        navigation<SubNavigation.HomeRoutes>(
            startDestination = Routes.HomeScreen
        ) {
            composable<Routes.HomeScreen> {
                HomeScreenUI(navController = navController)
            }
        }
    }
}

@Composable
fun HomeScreenUI(navController: NavHostController) {
}