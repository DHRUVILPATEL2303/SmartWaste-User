package com.example.smartwaste_user.presentation.navigation

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseInBack
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseInExpo
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.EaseOutExpo
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ClearAll
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.smartwaste_user.presentation.screens.Auth.LoginScreenUI
import com.example.smartwaste_user.presentation.screens.OnBoarding.OnBoardingScreenUI
import com.example.smartwaste_user.presentation.screens.Auth.SignUpScreenUI
import com.example.smartwaste_user.presentation.screens.home.HomeScreenUI
import com.example.smartwaste_user.presentation.screens.notification.NotificationScreenUI
import com.example.smartwaste_user.presentation.screens.profile.ProfileScreenUI
import com.example.smartwaste_user.presentation.screens.reportscreens.ExtraServiceScreen
import com.example.smartwaste_user.presentation.screens.reportscreens.MakeReportScreenUI
import com.example.smartwaste_user.presentation.screens.reportscreens.ReportScreenUI
import com.example.smartwaste_user.presentation.screens.verificationScreens.VerificationScreenUI
import com.example.smartwaste_user.presentation.viewmodels.AuthViewModel
import com.example.smartwaste_user.presentation.viewmodels.OnboardingViewModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class BottomBarItem(
    val name: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val color: Color = Color(0xFF2196F3)
)

val bottomBarItems = listOf(
    BottomBarItem("Home", Icons.Filled.Home, Icons.Outlined.Home, Color(0xFF2196F3)),
    BottomBarItem("Report", Icons.Filled.ClearAll, Icons.Outlined.ClearAll, Color(0xFF4CAF50)),
    BottomBarItem("Notify", Icons.Filled.Notifications, Icons.Outlined.Notifications, Color(0xFFFF9800)),
    BottomBarItem("Profile", Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle, Color(0xFF9C27B0)),
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
            AnimatedVisibility(
                visible = currentBaseRoute in bottomBarRoutes,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = 600,
                        easing = EaseInOutCubic
                    )
                ) + slideInVertically(
                    animationSpec = tween(
                        durationMillis = 600,
                        easing = EaseOutBack
                    ),
                    initialOffsetY = { it }
                ) + scaleIn(
                    animationSpec = tween(
                        durationMillis = 600,
                        easing = EaseOutBack
                    ),
                    initialScale = 0.8f
                ),
                exit = fadeOut(
                    animationSpec = tween(
                        durationMillis = 400,
                        easing = EaseInOutCubic
                    )
                ) + slideOutVertically(
                    animationSpec = tween(
                        durationMillis = 400,
                        easing = EaseInBack
                    ),
                    targetOffsetY = { it }
                ) + scaleOut(
                    animationSpec = tween(
                        durationMillis = 400,
                        easing = EaseInBack
                    ),
                    targetScale = 0.8f
                )
            ) {
                ProfessionalBottomBar(
                    selectedItem = selectedItem,
                    items = bottomBarItems,
                    onItemSelected = { index ->
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
                    }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
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
                .fillMaxSize()
             ,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = 400,
                        easing = FastOutSlowInEasing
                    )
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it / 2 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeOut(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = LinearOutSlowInEasing
                    )
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = 400,
                        easing = FastOutSlowInEasing
                    )
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeOut(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = LinearOutSlowInEasing
                    )
                )
            }
        ) {
            // Navigation graph remains the same
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
                    ReportScreenUI(navController = navController)
                }
                composable<Routes.NotificationScreen> {
                    NotificationScreenUI(navController)
                }
                composable<Routes.ProfileScreen> {
                    ProfileScreenUI(navController = navController)
                }
                composable<Routes.MakeReportScreen> {
                    MakeReportScreenUI(navController = navController)
                }
                composable<Routes.RequestExtraServiceScreen> {
                    ExtraServiceScreen(navController = navController)
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
fun ProfessionalBottomBar(
    selectedItem: Int,
    items: List<BottomBarItem>,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val barAlpha = remember { Animatable(0f) }
    val barTranslationY = remember { Animatable(20f) }

    LaunchedEffect(Unit) {
        launch {
            barAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 500,
                    easing = FastOutSlowInEasing
                )
            )
        }
        launch {
            barTranslationY.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .alpha(barAlpha.value)
            .offset(y = barTranslationY.value.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 8.dp,
            color = Color(0xFF1C1C1E), // Dark background for contrast
            tonalElevation = 6.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1C1C1E),
                                Color(0xFF2D2D2F)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEachIndexed { index, item ->
                        ProfessionalBottomBarItem(
                            item = item,
                            isSelected = selectedItem == index,
                            onClick = { onItemSelected(index) },
                            animationDelay = index * 50L
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfessionalBottomBarItem(
    item: BottomBarItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    animationDelay: Long = 0L
) {
    val scale = animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "item_scale"
    )

    val iconAlpha = animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.6f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "icon_alpha"
    )

    val itemAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(animationDelay)
        itemAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 400,
                easing = FastOutSlowInEasing
            )
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale.value)
            .alpha(itemAlpha.value)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(50.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = if (isSelected) {
                            Brush.radialGradient(
                                colors = listOf(
                                    item.color.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            )
                        } else {
                            Brush.radialGradient(
                                colors = listOf(Color.Transparent, Color.Transparent)
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = item.name,
                    tint = if (isSelected) item.color else Color.White.copy(alpha = iconAlpha.value),
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn(
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + slideInVertically(
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                initialOffsetY = { it / 2 }
            ),
            exit = fadeOut(
                animationSpec = tween(200, easing = LinearOutSlowInEasing)
            ) + slideOutVertically(
                animationSpec = tween(200, easing = LinearOutSlowInEasing),
                targetOffsetY = { it / 2 }
            )
        ) {
            Text(
                text = item.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = item.color,
                modifier = Modifier.offset(y = (-4).dp),
                letterSpacing = 0.4.sp
            )
        }
    }
}