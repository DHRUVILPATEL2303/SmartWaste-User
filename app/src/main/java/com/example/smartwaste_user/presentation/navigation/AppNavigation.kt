package com.example.smartwaste_user.presentation.navigation

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
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
import com.example.smartwaste_user.presentation.screens.profile.ProfileScreenUI
import com.example.smartwaste_user.presentation.screens.reportscreens.ExtraServiceScreen
import com.example.smartwaste_user.presentation.screens.reportscreens.MakeReportScreenUI
import com.example.smartwaste_user.presentation.screens.reportscreens.ReportScreenUI
import com.example.smartwaste_user.presentation.screens.verificationScreens.VerificationScreenUI
import com.example.smartwaste_user.presentation.viewmodels.AuthViewModel
import com.example.smartwaste_user.presentation.viewmodels.OnboardingViewModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay

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
                    animationSpec = tween(durationMillis = 400)
                ) + slideInVertically(
                    animationSpec = tween(durationMillis = 400),
                    initialOffsetY = { it }
                ),
                exit = fadeOut(
                    animationSpec = tween(durationMillis = 300)
                ) + slideOutVertically(
                    animationSpec = tween(durationMillis = 300),
                    targetOffsetY = { it }
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
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                fadeIn(
                    animationSpec = tween(durationMillis = 400)
                ) + slideInHorizontally(
                    animationSpec = tween(durationMillis = 400),
                    initialOffsetX = { it / 3 }
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(durationMillis = 300)
                ) + slideOutHorizontally(
                    animationSpec = tween(durationMillis = 300),
                    targetOffsetX = { -it / 3 }
                )
            }
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
                    ReportScreenUI(navController=navController)
                }
                composable<Routes.NotificationScreen> {
                    NotificationScreenUI(navController)
                }
                composable<Routes.ProfileScreen> {
                    ProfileScreenUI(navController=navController)
                }
                composable<Routes.MakeReportScreen> {
                    MakeReportScreenUI(navController=navController)
                }
                composable<Routes.RequestExtraServiceScreen> {
                    ExtraServiceScreen(navController=navController)
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
    val barScale = remember { Animatable(0f) }
    val barAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        barAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500)
        )

        barScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
            .scale(barScale.value)
            .alpha(barAlpha.value)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(78.dp),
            shape = RoundedCornerShape(34.dp),
            shadowElevation = 8.dp,
            color = Color(33, 32, 33, 255),
            tonalElevation = 6.dp
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
                        animationDelay = index * 100L
                    )
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
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "item_scale"
    )

    val backgroundAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(300),
        label = "background_alpha"
    )

    val itemAlpha = remember { Animatable(0f) }
    val itemScale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(animationDelay)
        itemAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400)
        )

        itemScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .scale(itemScale.value)
            .alpha(itemAlpha.value)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(52.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = if (isSelected) {
                            item.color.copy(alpha = 0.15f)
                        } else {
                            Color.Transparent
                        },
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = item.name,
                    tint = if (isSelected) {
                        item.color
                    } else {
                        Color.White.copy(alpha = 0.7f)
                    },
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn(
                animationSpec = tween(durationMillis = 300)
            ) + slideInVertically(
                animationSpec = tween(durationMillis = 300),
                initialOffsetY = { it / 2 }
            ),
            exit = fadeOut(
                animationSpec = tween(durationMillis = 200)
            ) + slideOutVertically(
                animationSpec = tween(durationMillis = 200),
                targetOffsetY = { it / 2 }
            )
        ) {
            Text(
                text = item.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = item.color,
                modifier = Modifier.offset(y = (-6).dp)
            )
        }
    }
}

@Composable
fun NotificationScreenUI(navController: NavHostController) {
    val containerAlpha = remember { Animatable(0f) }
    val containerScale = remember { Animatable(0.8f) }
    val iconScale = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        containerAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500)
        )

        containerScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )

        delay(200L)
        iconScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )

        delay(300L)
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2B432C)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(40, 40, 40, 240),
            shadowElevation = 12.dp,
            tonalElevation = 6.dp,
            modifier = Modifier
                .padding(24.dp)
                .scale(containerScale.value)
                .alpha(containerAlpha.value)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .scale(iconScale.value)
                        .background(
                            color = Color(0xFFFF9800).copy(alpha = 0.15f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Notifications",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.alpha(textAlpha.value)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Stay updated with latest alerts and updates",
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 20.sp,
                    modifier = Modifier.alpha(textAlpha.value)
                )
            }
        }
    }
}