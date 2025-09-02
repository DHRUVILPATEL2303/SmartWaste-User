package com.example.smartwaste_user.presentation.navigation

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.ClearAll
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.smartwaste_user.presentation.screens.Auth.LoginScreenUI
import com.example.smartwaste_user.presentation.screens.OnBoarding.OnBoardingScreenUI
import com.example.smartwaste_user.presentation.screens.Auth.SignUpScreenUI
import com.example.smartwaste_user.presentation.screens.chatbotscreen.ChatBotScreenUI
import com.example.smartwaste_user.presentation.screens.editorialscreen.EditorialScreenUI
import com.example.smartwaste_user.presentation.screens.home.HomeScreenUI
import com.example.smartwaste_user.presentation.screens.notification.NotificationScreenUI
import com.example.smartwaste_user.presentation.screens.profile.ProfileScreenUI
import com.example.smartwaste_user.presentation.screens.reportscreens.ExtraServiceScreen
import com.example.smartwaste_user.presentation.screens.reportscreens.MakeReportScreenUI
import com.example.smartwaste_user.presentation.screens.reportscreens.ReportScreenUI
import com.example.smartwaste_user.presentation.screens.routemap.RouteMapScreenUI
import com.example.smartwaste_user.presentation.screens.verificationScreens.VerificationScreenUI
import com.example.smartwaste_user.presentation.viewmodels.AuthViewModel
import com.example.smartwaste_user.presentation.viewmodels.OnboardingViewModel
import com.google.firebase.auth.FirebaseUser

private const val PAGE_TRANSITION_DURATION = 400
private val pageTransitionSpec = tween<IntOffset>(durationMillis = PAGE_TRANSITION_DURATION)
private val fadeTransitionSpec = tween<Float>(durationMillis = PAGE_TRANSITION_DURATION)

private const val BOTTOM_BAR_ANIM_DURATION = 500
private val bottomBarSpringSpec = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium
)


object NavigationDarkTheme {
    val background = Color(0xFF0D1117)
    val surface = Color(0xFF161B22)
    val surfaceVariant = Color(0xFF21262D)
    val primary = Color(0xFF2ECC71)
    val secondary = Color(0xFF58A6FF)
    val accent = Color(0xFFE3B341)
    val error = Color(0xFFFF6B6B)
    val success = Color(0xFF56D364)
    val warning = Color(0xFFFFA726)
    val textPrimary = Color(0xFFE6EDF3)
    val textSecondary = Color(0xFF8B949E)
    val cardBackground = Color(0xFF1C2128)
    val borderColor = Color(0xFFE6E9ED)
    val glassEffect = Color(0x80FFFFFF)
}


data class BottomBarItem(
    val name: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val color: Color
)

val bottomBarItems = listOf(
    BottomBarItem("Home", Routes.HomeScreen::class.qualifiedName!!, Icons.Filled.Home, Icons.Outlined.Home, NavigationDarkTheme.primary),
    BottomBarItem("Report", Routes.ReportScreen::class.qualifiedName!!, Icons.Filled.ClearAll, Icons.Outlined.ClearAll, NavigationDarkTheme.success),
    BottomBarItem("Notify", Routes.NotificationScreen::class.qualifiedName!!, Icons.Filled.Notifications, Icons.Outlined.Notifications, NavigationDarkTheme.warning),
    BottomBarItem("ChatBot", Routes.ChatBotScreen::class.qualifiedName!!, Icons.Filled.Chat, Icons.Outlined.Chat, NavigationDarkTheme.error),
    BottomBarItem("Profile", Routes.ProfileScreen::class.qualifiedName!!, Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle, NavigationDarkTheme.secondary),
)

private val bottomBarRoutes = bottomBarItems.map { it.route }


// --- MAIN NAVIGATION COMPOSABLE ---
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
        else -> SubNavigation.VerifyEmailRoutes
    }

    Log.d("AppNavigation", "startDestination: $startDestination, isEmailVerified: ${currentUser?.isEmailVerified}")

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentBaseRoute = currentRoute?.substringBefore("?")?.substringBefore("/")

    val isBottomBarVisible = currentBaseRoute in bottomBarRoutes

    var selectedItemIndex by remember(currentBaseRoute) {
        mutableIntStateOf(bottomBarItems.indexOfFirst { it.route == currentBaseRoute }.coerceAtLeast(0))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(colors = listOf(NavigationDarkTheme.background, NavigationDarkTheme.surface))
            )
    ) {
        Scaffold(
            bottomBar = {
                AnimatedVisibility(
                    visible = isBottomBarVisible,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(durationMillis = BOTTOM_BAR_ANIM_DURATION)
                    ) + fadeIn(animationSpec = tween(durationMillis = BOTTOM_BAR_ANIM_DURATION)),
                    exit = slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(durationMillis = BOTTOM_BAR_ANIM_DURATION)
                    ) + fadeOut(animationSpec = tween(durationMillis = BOTTOM_BAR_ANIM_DURATION))
                ) {
                    ModernBottomNavigationBar(
                        selectedItemIndex = selectedItemIndex,
                        items = bottomBarItems,
                        onItemSelected = { index ->
                            if (selectedItemIndex != index) {
                                selectedItemIndex = index
                                navController.navigate(bottomBarItems[index].route) {
                                    popUpTo(Routes.HomeScreen::class.qualifiedName!!) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            },
            containerColor = Color.Transparent // Make scaffold transparent
        ) { paddingValues ->
            BackHandler(enabled = isBottomBarVisible && currentBaseRoute != Routes.HomeScreen::class.qualifiedName) {
                navController.navigate(Routes.HomeScreen::class.qualifiedName!!) {
                    popUpTo(Routes.HomeScreen::class.qualifiedName!!) { inclusive = false }
                    launchSingleTop = true
                }
                selectedItemIndex = 0
            }


            val blurRadius by animateDpAsState(
                targetValue = if (isBottomBarVisible) 12.dp else 0.dp,
                animationSpec = tween(durationMillis = BOTTOM_BAR_ANIM_DURATION),
                label = "blur_animation"
            )

            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { contentDescription = "Main navigation" },
                enterTransition = {
                    slideInHorizontally(initialOffsetX = { 300 }, animationSpec = pageTransitionSpec) + fadeIn(animationSpec = fadeTransitionSpec)
                },
                exitTransition = {
                    slideOutHorizontally(targetOffsetX = { -300 }, animationSpec = pageTransitionSpec) + fadeOut(animationSpec = fadeTransitionSpec)
                },
                popEnterTransition = {
                    slideInHorizontally(initialOffsetX = { -300 }, animationSpec = pageTransitionSpec) + fadeIn(animationSpec = fadeTransitionSpec)
                },
                popExitTransition = {
                    slideOutHorizontally(targetOffsetX = { 300 }, animationSpec = pageTransitionSpec) + fadeOut(animationSpec = fadeTransitionSpec)
                }
            ) {
                // --- Your navigation graph definitions remain the same ---
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
                    composable<Routes.RouteMapScreen> {
                        val data = it.toRoute<Routes.RouteMapScreen>()
                        RouteMapScreenUI(navController = navController, routeId = data.routeId)
                    }
                    composable<Routes.ChatBotScreen> { ChatBotScreenUI(navController = navController) }
                    composable<Routes.EditorialScreen> { EditorialScreenUI(navController = navController) }
                }
                navigation<SubNavigation.VerifyEmailRoutes>(startDestination = Routes.VerifyEmailScreen) {
                    composable<Routes.VerifyEmailScreen> {
                        VerificationScreenUI(navController, currentUser!!)
                    }
                }
            }
        }
    }
}

// --- MODERN BOTTOM NAVIGATION BAR ---
@Composable
fun ModernBottomNavigationBar(
    selectedItemIndex: Int,
    items: List<BottomBarItem>,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .navigationBarsPadding(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            NavigationDarkTheme.borderColor,
                           Color(0xFF448AFF)
                        ),
                        tileMode = TileMode.Repeated
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                ModernBottomNavItem(
                    item = item,
                    isSelected = selectedItemIndex == index,
                    onClick = { onItemSelected(index) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}


@Composable
fun ModernBottomNavItem(
    item: BottomBarItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) item.color.copy(alpha = 0.1f) else Color.Transparent,
        animationSpec = spring(),
        label = "background_color_anim"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
        label = "scale_anim"
    )

    // Animation for the icon color
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) item.color else NavigationDarkTheme.textSecondary,
        animationSpec = tween(300),
        label = "icon_color_anim"
    )

    val iconSize by animateDpAsState(
        targetValue = if (isSelected) 26.dp else 22.dp,
        animationSpec = spring(),
        label = "icon_size_anim"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
        ) {
            Icon(
                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.name,
                tint = iconColor,
                modifier = Modifier.size(iconSize)
            )

            // Animated label for selected items
            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(animationSpec = tween(200, 100)) + expandVertically(animationSpec = spring()),
                exit = fadeOut(animationSpec = tween(200)) + shrinkVertically(animationSpec = spring())
            ) {
                Text(
                    text = item.name,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = item.color,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}