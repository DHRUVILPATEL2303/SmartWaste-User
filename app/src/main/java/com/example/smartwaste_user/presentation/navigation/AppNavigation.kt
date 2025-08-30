package com.example.smartwaste_user.presentation.navigation

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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

const val PAGE_TRANSITION_DURATION = 500
const val BOTTOM_BAR_ANIM_DURATION = 600

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
    val borderColor = Color(0xFF30363D)
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
    BottomBarItem(
        "Home",
        Routes.HomeScreen::class.qualifiedName!!,
        Icons.Filled.Home,
        Icons.Outlined.Home,
        NavigationDarkTheme.primary
    ),
    BottomBarItem(
        "Report",
        Routes.ReportScreen::class.qualifiedName!!,
        Icons.Filled.ClearAll,
        Icons.Outlined.ClearAll,
        NavigationDarkTheme.success
    ),
    BottomBarItem(
        "Notify",
        Routes.NotificationScreen::class.qualifiedName!!,
        Icons.Filled.Notifications,
        Icons.Outlined.Notifications,
        NavigationDarkTheme.warning
    ),
    BottomBarItem(
        name="ChatBot",
        Routes.ChatBotScreen::class.qualifiedName!!,
        Icons.Filled.Chat,
        Icons.Outlined.Chat,
        NavigationDarkTheme.error
    ),
    BottomBarItem(
        "Profile",
        Routes.ProfileScreen::class.qualifiedName!!,
        Icons.Filled.AccountCircle,
        Icons.Outlined.AccountCircle,
        NavigationDarkTheme.secondary
    ),
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

    Log.d(
        "AppNavigation",
        "startDestination: $startDestination, isEmailVerified: ${currentUser?.isEmailVerified}"
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentBaseRoute = currentRoute?.substringBefore("?")?.substringBefore("/")

    var selectedItemIndex by remember { mutableIntStateOf(0) }
    val currentIndex = bottomBarItems.indexOfFirst { it.route == currentBaseRoute }
    if (currentIndex != -1) {
        selectedItemIndex = currentIndex
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        NavigationDarkTheme.background,
                        NavigationDarkTheme.surface
                    )
                )
            )
    ) {
        Scaffold(
            bottomBar = {
                AnimatedVisibility(
                    visible = currentBaseRoute in bottomBarRoutes,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) + fadeIn(
                        animationSpec = tween(
                            durationMillis = BOTTOM_BAR_ANIM_DURATION,
                            easing = EaseInOutCubic
                        )
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) + fadeOut(
                        animationSpec = tween(
                            durationMillis = BOTTOM_BAR_ANIM_DURATION,
                            easing = EaseInOutCubic
                        )
                    )
                ) {
                    ModernBottomNavigationBar(
                        selectedItemIndex = selectedItemIndex,
                        items = bottomBarItems,
                        onItemSelected = { index ->
                            if (selectedItemIndex != index) {
                                selectedItemIndex = index
                                navController.navigate(bottomBarItems[index].route) {
                                    popUpTo(Routes.HomeScreen::class.qualifiedName!!) {
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
            containerColor = Color.Transparent // Make scaffold transparent
        ) { paddingValues ->
            BackHandler(enabled = currentBaseRoute in bottomBarRoutes && currentBaseRoute != Routes.HomeScreen::class.qualifiedName) {
                navController.navigate(Routes.HomeScreen) {
                    popUpTo(Routes.HomeScreen::class.qualifiedName!!) { inclusive = false }
                    launchSingleTop = true
                }
                selectedItemIndex = 0
            }


            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .semantics { contentDescription = "Main navigation" },
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                            visibilityThreshold = IntOffset.VisibilityThreshold
                        )
                    ) + fadeIn(
                        animationSpec = tween(
                            durationMillis = 350,
                            delayMillis = 50,
                            easing = FastOutSlowInEasing
                        )
                    ) + scaleIn(
                        initialScale = 0.95f,
                        animationSpec = tween(
                            durationMillis = 350,
                            easing = FastOutSlowInEasing
                        )
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth / 3 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                            visibilityThreshold = IntOffset.VisibilityThreshold
                        )
                    ) + fadeOut(
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = FastOutLinearInEasing
                        )
                    ) + scaleOut(
                        targetScale = 0.95f,
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = FastOutLinearInEasing
                        )
                    )
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> -fullWidth / 3 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                            visibilityThreshold = IntOffset.VisibilityThreshold
                        )
                    ) + fadeIn(
                        animationSpec = tween(
                            durationMillis = 350,
                            delayMillis = 30,
                            easing = FastOutSlowInEasing
                        )
                    ) + scaleIn(
                        initialScale = 0.95f,
                        animationSpec = tween(
                            durationMillis = 350,
                            easing = FastOutSlowInEasing
                        )
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                            visibilityThreshold = IntOffset.VisibilityThreshold
                        )
                    ) + fadeOut(
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = FastOutLinearInEasing
                        )
                    ) + scaleOut(
                        targetScale = 0.98f,
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = FastOutLinearInEasing
                        )
                    )
                }
            ) {
                navigation<SubNavigation.OnBoardingScreen>(startDestination = Routes.OnBoardingScreen) {
                    composable<Routes.OnBoardingScreen> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(NavigationDarkTheme.background)
                        ) {
                            OnBoardingScreenUI(navController)
                        }
                    }
                }
                navigation<SubNavigation.AuthRoutes>(startDestination = Routes.LoginScreen) {
                    composable<Routes.LoginScreen> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(NavigationDarkTheme.background)
                        ) {
                            LoginScreenUI(navController = navController)
                        }
                    }
                    composable<Routes.SignUpScreen> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(NavigationDarkTheme.background)
                        ) {
                            SignUpScreenUI(navController = navController)
                        }
                    }
                }
                navigation<SubNavigation.HomeRoutes>(startDestination = Routes.HomeScreen) {
                    composable<Routes.HomeScreen> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(NavigationDarkTheme.background)
                        ) {
                            HomeScreenUI(navController)
                        }
                    }
                    composable<Routes.ReportScreen> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(NavigationDarkTheme.background)
                        ) {
                            ReportScreenUI(navController = navController)
                        }
                    }
                    composable<Routes.NotificationScreen> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(NavigationDarkTheme.background)
                        ) {
                            NotificationScreenUI(navController = navController)
                        }
                    }
                    composable<Routes.ProfileScreen> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(NavigationDarkTheme.background)
                        ) {
                            ProfileScreenUI(navController = navController)
                        }
                    }
                    composable<Routes.MakeReportScreen> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(NavigationDarkTheme.background)
                        ) {
                            MakeReportScreenUI(navController = navController)
                        }
                    }
                    composable<Routes.RequestExtraServiceScreen> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(NavigationDarkTheme.background)
                        ) {
                            ExtraServiceScreen(navController = navController)
                        }
                    }
                    composable<Routes.RouteMapScreen> {
                        val data = it.toRoute<Routes.RouteMapScreen>()
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(NavigationDarkTheme.background)
                        ) {
                            RouteMapScreenUI(
                                navController = navController,
                                routeId = data.routeId
                            )
                        }
                    }
                    composable<Routes.ChatBotScreen> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(NavigationDarkTheme.background)
                        ) {
                            ChatBotScreenUI(navController = navController)
                        }
                    }
                    composable<Routes.EditorialScreen> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(NavigationDarkTheme.background)
                        ) {
                            EditorialScreenUI(navController = navController)
                        }
                    }
                }
                navigation<SubNavigation.VerifyEmailRoutes>(startDestination = Routes.VerifyEmailScreen) {
                    composable<Routes.VerifyEmailScreen> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(NavigationDarkTheme.background)
                        ) {
                            VerificationScreenUI(
                                navController,
                                currentUser!!
                            )
                        }
                    }
                }
            }
        }
    }
}

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
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .navigationBarsPadding(),

        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "border_animation")


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(
                    Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(28.dp)
                )

        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
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
}

@Composable
fun ModernBottomNavItem(
    item: BottomBarItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateDpAsState(
        targetValue = if (isSelected) 1.2.dp else 1.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale_animation"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) item.color else NavigationDarkTheme.textSecondary,
        animationSpec = tween(400, easing = EaseInOutCubic),
        label = "icon_color"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(56.dp)

            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.scale(scale.value)
        ) {
            Box(
                modifier = Modifier
                    .size(if (isSelected) 32.dp else 28.dp)
                    .background(
                        if (isSelected) item.color.copy(alpha = 0.15f) else Color.Transparent,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = item.name,
                    modifier = Modifier.size(if (isSelected) 26.dp else 22.dp)
                )
            }

            // Animated label
            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(
                    animationSpec = tween(300, delayMillis = 100)
                ) + slideInVertically(
                    animationSpec = tween(300, delayMillis = 100)
                ) { it / 2 },
                exit = fadeOut(
                    animationSpec = tween(200)
                ) + slideOutVertically(
                    animationSpec = tween(200)
                ) { it / 2 }
            ) {
                Text(
                    text = item.name,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = item.color,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (!isSelected) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(4.dp)
                        .background(
                            NavigationDarkTheme.textSecondary.copy(alpha = 0.5f),
                            CircleShape
                        )
                )
            }
        }
    }
}