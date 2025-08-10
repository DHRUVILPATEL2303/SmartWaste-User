package com.example.smartwaste_user.presentation.navigation

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.bottombar.AnimatedBottomBar
import com.example.bottombar.components.BottomBarItem
import com.example.bottombar.model.IndicatorDirection
import com.example.bottombar.model.IndicatorStyle
import com.example.smartwaste_user.presentation.screens.Auth.LoginScreenUI
import com.example.smartwaste_user.presentation.screens.OnBoarding.OnBoardingScreenUI
import com.example.smartwaste_user.presentation.screens.Auth.SignUpScreenUI
import com.example.smartwaste_user.presentation.screens.home.HomeScreenUI
import com.example.smartwaste_user.presentation.screens.verificationScreens.VerificationScreenUI
import com.example.smartwaste_user.presentation.viewmodels.AuthViewModel
import com.example.smartwaste_user.presentation.viewmodels.OnboardingViewModel
import com.google.firebase.auth.FirebaseUser



data class BottomBarItem(
    val name: String,
    val icon: ImageVector
)
val bottomBarItems = listOf(
    BottomBarItem("Home", Icons.Default.Home),
    BottomBarItem("Report", Icons.Default.ClearAll),
    BottomBarItem("Notify", Icons.Default.Star),
    BottomBarItem("Profile", Icons.Default.AccountCircle),
)

val bottomBarRoutes = listOf(
    Routes.HomeScreen::class.qualifiedName,
    Routes.ReportScreen::class.qualifiedName,
    Routes.NotificationScreen::class.qualifiedName,
    Routes.ProfileScreen::class.qualifiedName,
)

@Composable
fun AppNavigation(
    viewModel: AuthViewModel = hiltViewModel<AuthViewModel>(),
    onboardingViewModel: OnboardingViewModel = hiltViewModel<OnboardingViewModel>(),
    shouldShowOnboarding: Boolean,
    currentUser: FirebaseUser?,
) {
    val navController = rememberNavController()
    val isOnboardingCompleted by onboardingViewModel.onboardingCompleted.collectAsState(initial = false)

    val startDestination = when {
        shouldShowOnboarding -> SubNavigation.OnBoardingScreen
        currentUser == null -> SubNavigation.AuthRoutes
        currentUser.isEmailVerified-> SubNavigation.HomeRoutes
        !currentUser.isEmailVerified -> SubNavigation.VerifyEmailRoutes

        else -> SubNavigation.VerifyEmailRoutes

    }

    Log.d("AppNavigation", "startDestination: $startDestination, isEmailVerified: ${currentUser?.isEmailVerified}")

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentBaseRoute = currentRoute?.substringBefore("?")?.substringBefore("/")

    var selectedItem by remember { mutableIntStateOf(0) }
    val currentIndex = bottomBarRoutes.indexOf(currentBaseRoute)
    if (currentIndex != -1) selectedItem = currentIndex

    Scaffold(
        bottomBar = {
            if (currentBaseRoute in bottomBarRoutes) {
                AnimatedBottomBar(
                    selectedItem = selectedItem,
                    itemSize = bottomBarItems.size,
                    containerColor = Color(46, 60, 74).copy(alpha = 0.6f),
                    indicatorStyle = IndicatorStyle.FILLED,
                    containerShape = RoundedCornerShape(50.dp),
                    bottomBarHeight = 65.dp,
                    modifier = Modifier.padding(16.dp).navigationBarsPadding(),
                    indicatorColor = Color.White.copy(alpha = 0.4f),
                    indicatorDirection = IndicatorDirection.BOTTOM
                ) {
                    bottomBarItems.forEachIndexed { index, item ->
                        BottomBarItem(
                            selected = selectedItem == index,
                            onClick = {
                                if (selectedItem != index) {
                                    selectedItem = index
                                    val route = when (index) {
                                        0 -> Routes.HomeScreen
                                        1 -> Routes.ReportScreen
                                        2 -> Routes.NotificationScreen
                                        3 -> Routes.ProfileScreen
                                        else -> Routes.HomeScreen
                                    }
                                    navController.navigate(route) {
                                        popUpTo(Routes.HomeScreen) {
                                            inclusive = false
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            imageVector = item.icon,
                            iconColor = if (selectedItem == index) Color.Red else Color.White,
                            label = item.name,
                            contentColor = Color.Red,
                            textColor = Color.White
                        )
                    }
                }
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        BackHandler(enabled = currentBaseRoute in bottomBarRoutes && currentBaseRoute != Routes.HomeScreen::class.qualifiedName) {
            navController.navigate(Routes.HomeScreen) {
                popUpTo(Routes.HomeScreen) { inclusive = false }
                launchSingleTop = true
            }
            selectedItem = 0
        }

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
//                .padding(paddingValues)
                .fillMaxSize()
        ) {
            navigation<SubNavigation.OnBoardingScreen>(startDestination = Routes.OnBoardingScreen) {
                composable<Routes.OnBoardingScreen> {
                    OnBoardingScreenUI(navController)
                }
            }
            navigation<SubNavigation.AuthRoutes>(startDestination = Routes.LoginScreen) {
                composable<Routes.LoginScreen> {
                    LoginScreenUI(navController = navController)
                }
                composable<Routes.SignUpScreen> {
                    SignUpScreenUI(navController = navController)
                }
            }
            navigation<SubNavigation.HomeRoutes>(startDestination = Routes.HomeScreen) {
                composable<Routes.HomeScreen> {
                    HomeScreenUI(navController)
                }
                composable<Routes.ReportScreen> {
                    ReportScreenUI(navController)
                }
                composable<Routes.NotificationScreen> {
                    NotificationScreenUI(navController)
                }
                composable<Routes.ProfileScreen> {
                    ProfileScreenUI(navController)
                }
            }
            navigation<SubNavigation.VerifyEmailRoutes>(startDestination = Routes.VerifyEmailScreen) {
                composable<Routes.VerifyEmailScreen> {
                    VerificationScreenUI(navController, currentUser!!)
                }
            }
        }
    }
}

@Composable
fun ProfileScreenUI(navController: NavHostController) {

}

@Composable
fun NotificationScreenUI(navController: NavHostController) {

}

@Composable
fun ReportScreenUI(navController: NavHostController) {

}




