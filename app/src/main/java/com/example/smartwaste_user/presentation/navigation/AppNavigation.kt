package com.example.smartwaste_user.presentation.navigation

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.navigationBarsPadding

const val PAGE_TRANSITION_DURATION = 400
const val BOTTOM_BAR_ANIM_DURATION = 500

data class BottomBarItem(
    val name: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val color: Color
)

val bottomBarItems = listOf(
    BottomBarItem("Home", Routes.HomeScreen::class.qualifiedName!!, Icons.Filled.Home, Icons.Outlined.Home, Color(0xFF2196F3)),
    BottomBarItem("Report", Routes.ReportScreen::class.qualifiedName!!, Icons.Filled.ClearAll, Icons.Outlined.ClearAll, Color(0xFF4CAF50)),
    BottomBarItem("Notify", Routes.NotificationScreen::class.qualifiedName!!, Icons.Filled.Notifications, Icons.Outlined.Notifications, Color(0xFFFF9800)),
    BottomBarItem("Profile", Routes.ProfileScreen::class.qualifiedName!!, Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle, Color(0xFF9C27B0)),
)
private val bottomBarRoutes = bottomBarItems.map { it.route }

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
        currentUser.isEmailVerified -> SubNavigation.HomeRoutes
        !currentUser.isEmailVerified -> SubNavigation.VerifyEmailRoutes
        else -> SubNavigation.VerifyEmailRoutes
    }

    Log.d("AppNavigation", "startDestination: $startDestination, isEmailVerified: ${currentUser?.isEmailVerified}")

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentBaseRoute = currentRoute?.substringBefore("?")?.substringBefore("/")

    var selectedItemIndex by remember { mutableIntStateOf(0) }
    val currentIndex = bottomBarItems.indexOfFirst { it.route == currentBaseRoute }
    if (currentIndex != -1) {
        selectedItemIndex = currentIndex
    }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = currentBaseRoute in bottomBarRoutes,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = BOTTOM_BAR_ANIM_DURATION, easing = EaseInOutCubic)
                ) + fadeIn(animationSpec = tween(durationMillis = BOTTOM_BAR_ANIM_DURATION)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = BOTTOM_BAR_ANIM_DURATION, easing = EaseInOutCubic)
                ) + fadeOut(animationSpec = tween(durationMillis = BOTTOM_BAR_ANIM_DURATION))
            ) {
                SmoothBottomBar(
                    selectedItemIndex = selectedItemIndex,
                    items = bottomBarItems,
                    onItemSelected = { index ->
                        if (selectedItemIndex != index) {
                            selectedItemIndex = index
                            navController.navigate(bottomBarItems[index].route) {
                                popUpTo(Routes.HomeScreen) {
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
            selectedItemIndex = 0
        }

        NavHost(
            navController = navController,
            startDestination = startDestination,

            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(PAGE_TRANSITION_DURATION, easing = EaseInOutCubic)
                ) + fadeIn(tween(PAGE_TRANSITION_DURATION))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(PAGE_TRANSITION_DURATION, easing = EaseInOutCubic)
                ) + fadeOut(tween(PAGE_TRANSITION_DURATION))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(PAGE_TRANSITION_DURATION, easing = EaseInOutCubic)
                ) + fadeIn(tween(PAGE_TRANSITION_DURATION))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(PAGE_TRANSITION_DURATION, easing = EaseInOutCubic)
                ) + fadeOut(tween(PAGE_TRANSITION_DURATION))
            }
        ) {
            navigation<SubNavigation.OnBoardingScreen>(startDestination = Routes.OnBoardingScreen) {
                composable<Routes.OnBoardingScreen> { OnBoardingScreenUI(navController) }
            }
            navigation<SubNavigation.AuthRoutes>(startDestination = Routes.LoginScreen) {
                composable<Routes.LoginScreen> { LoginScreenUI(navController = navController) }
                composable<Routes.SignUpScreen> { SignUpScreenUI(navController = navController) }
            }
            navigation<SubNavigation.HomeRoutes>(startDestination = Routes.HomeScreen) {
                composable<Routes.HomeScreen> { HomeScreenUI(navController) }
                composable<Routes.ReportScreen> { ReportScreenUI(navController = navController) }
                composable<Routes.NotificationScreen> { NotificationScreenUI(navController = navController) }
                composable<Routes.ProfileScreen> { ProfileScreenUI(navController = navController) }
                composable<Routes.MakeReportScreen> { MakeReportScreenUI(navController = navController) }
                composable<Routes.RequestExtraServiceScreen> { ExtraServiceScreen(navController = navController) }
            }
            navigation<SubNavigation.VerifyEmailRoutes>(startDestination = Routes.VerifyEmailScreen) {
                composable<Routes.VerifyEmailScreen> { VerificationScreenUI(navController, currentUser!!) }
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun SmoothBottomBar(
    selectedItemIndex: Int,
    items: List<BottomBarItem>,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .navigationBarsPadding(),
        shape = CircleShape,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color.Black)

        ) {
            val itemWidth = maxWidth / items.size

            val indicatorOffset by animateDpAsState(
                targetValue = itemWidth * selectedItemIndex,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "indicator_offset"
            )

            val indicatorColor by animateColorAsState(
                targetValue = items[selectedItemIndex].color.copy(alpha = 0.2f),
                animationSpec = tween(400, easing = EaseInOutCubic),
                label = "indicator_color"
            )

            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(itemWidth)
                    .fillMaxSize()
                    .padding(6.dp)
                    .clip(CircleShape)
                    .background(indicatorColor)
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    SmoothBottomBarItem(
                        modifier = Modifier.width(itemWidth),
                        item = item,
                        isSelected = selectedItemIndex == index,
                        onClick = { onItemSelected(index) }
                    )
                }
            }
        }
    }
}

@Composable
fun SmoothBottomBarItem(
    modifier: Modifier = Modifier,
    item: BottomBarItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) item.color else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300, easing = EaseInOutCubic),
        label = "icon_color"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
            contentDescription = item.name,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )

        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn(tween(200, 100)) + slideInVertically(tween(200, 100)) { it / 2 },
            exit = fadeOut(tween(150)) + slideOutVertically(tween(150)) { it / 2 }
        ) {
            Text(
                text = item.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = item.color,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}