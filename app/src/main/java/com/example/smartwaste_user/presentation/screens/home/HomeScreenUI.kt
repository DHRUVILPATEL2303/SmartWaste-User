package com.example.smartwaste_user.presentation.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.smartwaste_user.data.models.AreaProgress
import com.example.smartwaste_user.data.models.RouteProgressModel
import com.example.smartwaste_user.data.models.UserModel
import com.example.smartwaste_user.data.models.WorkerFeedBackModel
import com.example.smartwaste_user.presentation.viewmodels.CommonRoutesProgressState
import com.example.smartwaste_user.presentation.viewmodels.RouteProgressViewModel
import com.example.smartwaste_user.presentation.viewmodels.UserViewModel
import com.example.smartwaste_user.presentation.viewmodels.WorkerFeedBackViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenUI(
    navController: NavHostController,
    userViewModel: UserViewModel = hiltViewModel<UserViewModel>(),
    routeProgressViewModel: RouteProgressViewModel = hiltViewModel<RouteProgressViewModel>(),
    workerFeedBackViewModel: WorkerFeedBackViewModel = hiltViewModel<WorkerFeedBackViewModel>()
) {
    val userState by userViewModel.userState.collectAsState()
    var showVerifyDialog by remember { mutableStateOf(false) }
    var isVerified by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null && !user.isEmailVerified) {
            showVerifyDialog = true
        }
        routeProgressViewModel.getallRouteProgress()
    }

    val feedBackState by workerFeedBackViewModel.workerFeedBackState.collectAsState()
    LaunchedEffect(feedBackState) {
        if (feedBackState.success != null) {
            snackbarHostState.showSnackbar(
                message = feedBackState.success!!,
                duration = SnackbarDuration.Short
            )
        }
        if (feedBackState.error.isNotEmpty()) {
            snackbarHostState.showSnackbar(
                message = "Feedback Error: ${feedBackState.error}",
                duration = SnackbarDuration.Long
            )
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SleekHomeTopAppBar(isAreaSelected = userState.succcess?.areaId?.isNotEmpty() == true)
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
        ) {
            when {
                userState.isLoading -> ModernLoadingState("Fetching your details...")
                userState.error.isNotEmpty() -> ModernErrorState(message = userState.error)
                userState.succcess != null -> {
                    val user = userState.succcess!!
                    if (user.areaId.isEmpty() || user.areaName.isEmpty()) {
                        ModernAreaSelectionScreen(
                            routeProgressState = routeProgressViewModel.routeProgressState.collectAsState().value,
                            onAreaSelected = { routeId, routeName, areaId, areaName ->
                                userViewModel.updateUserData(
                                    user.copy(
                                        routeId = routeId,
                                        routeName = routeName,
                                        areaId = areaId,
                                        areaName = areaName
                                    )
                                )
                            }
                        )
                    } else {
                        ModernRouteStatusScreen(
                            user = user,
                            routeProgressState = routeProgressViewModel.routeProgressState.collectAsState().value,
                            workerFeedBackViewModel = workerFeedBackViewModel
                        )
                    }
                }
            }
        }

        if (showVerifyDialog && !isVerified) {
            ModernEmailVerificationDialog(
                onDismiss = { /* Optional */ },
                onRefresh = {
                    val user = FirebaseAuth.getInstance().currentUser
                    user?.reload()?.addOnCompleteListener {
                        if (user.isEmailVerified) {
                            isVerified = true
                            showVerifyDialog = false
                        }
                    }
                },
                onResend = { FirebaseAuth.getInstance().currentUser?.sendEmailVerification() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernHomeTopAppBarCompact(
    isAreaSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_offset"
    )

    val gradientColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.9f),
        MaterialTheme.colorScheme.primary
    )

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                val iconScale by animateFloatAsState(
                    targetValue = if (isAreaSelected) 1.1f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "icon_scale"
                )

                Box(
                    modifier = Modifier
                        .scale(iconScale)
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            Color.White.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.Default.FireTruck,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = if (isAreaSelected) "Collection Status" else "SmartWaste",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color.White,
                        modifier = Modifier.animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy
                            )
                        )
                    )

                    AnimatedVisibility(
                        visible = isAreaSelected,
                        enter = fadeIn(animationSpec = tween(600)) +
                                slideInVertically(
                                    initialOffsetY = { -it },
                                    animationSpec = tween(600)
                                ),
                        exit = fadeOut(animationSpec = tween(400)) +
                                slideOutVertically(
                                    targetOffsetY = { -it },
                                    animationSpec = tween(400)
                                )
                    ) {
                        Text(
                            text = "Real-time Updates",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            if (isAreaSelected) Color(0xFF4CAF50) else Color(0xFFFF9800),
                            CircleShape
                        )
                        .then(
                            if (isAreaSelected) {
                                Modifier.drawBehind {
                                    val rippleRadius = size.width * (1f + animatedOffset * 0.8f)
                                    drawCircle(
                                        color = Color(0xFF4CAF50).copy(alpha = 0.3f * (1f - animatedOffset)),
                                        radius = rippleRadius,
                                        center = center
                                    )
                                }
                            } else Modifier
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Color.White.copy(alpha = 0.3f),
                                CircleShape
                            )
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),
        modifier = modifier
            .height(78.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = gradientColors,
                    startX = animatedOffset * 600f,
                    endX = (animatedOffset + 1f) * 600f
                ),
                shape = RoundedCornerShape(
                    bottomStart = 28.dp,
                    bottomEnd = 28.dp
                )
            )
            .drawBehind {
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.15f)
                        )
                    ),
                    cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx()),
                    topLeft = Offset(0f, size.height - 6.dp.toPx()),
                    size = Size(size.width, 6.dp.toPx())
                )

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    ),
                    cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx()),
                    size = Size(size.width, size.height * 0.4f)
                )

                drawRoundRect(
                    color = Color.White.copy(alpha = 0.1f),
                    cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            .padding(horizontal = 20.dp),
        windowInsets = WindowInsets(top = 32.dp)
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassmorphismTopAppBar(
    isAreaSelected: Boolean,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Color.White.copy(alpha = 0.15f),
                            RoundedCornerShape(14.dp)
                        )
                        .border(
                            1.dp,
                            Color.White.copy(alpha = 0.25f),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(10.dp)
                ) {
                    Icon(
                        Icons.Default.FireTruck,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(18.dp))

                Column {
                    Text(
                        text = if (isAreaSelected) "Collection Status" else "SmartWaste",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.3.sp
                        ),
                        color = Color.White
                    )

                    AnimatedVisibility(
                        visible = isAreaSelected,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        Text(
                            text = "Live Tracking Active",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    modifier = Modifier
                        .background(
                            Color.White.copy(alpha = 0.12f),
                            RoundedCornerShape(18.dp)
                        )
                        .border(
                            0.5.dp,
                            Color.White.copy(alpha = 0.2f),
                            RoundedCornerShape(18.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    color = Color.Transparent
                ) {
                    Text(
                        text = if (isAreaSelected) "ACTIVE" else "SETUP",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = if (isAreaSelected) Color(0xFF4CAF50) else Color(0xFFFF9800),
                        fontSize = 9.sp
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),
        modifier = modifier
            .height(80.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
                    )
                ),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .drawBehind {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.15f),
                    cornerRadius = CornerRadius(32.dp.toPx(), 32.dp.toPx()),
                    style = Stroke(width = 1.5.dp.toPx())
                )

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    ),
                    cornerRadius = CornerRadius(32.dp.toPx(), 32.dp.toPx()),
                    size = Size(size.width, size.height * 0.4f)
                )

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.1f)
                        )
                    ),
                    cornerRadius = CornerRadius(32.dp.toPx(), 32.dp.toPx()),
                    topLeft = Offset(0f, size.height - 8.dp.toPx()),
                    size = Size(size.width, 8.dp.toPx())
                )
            }
            .padding(horizontal = 22.dp),
        windowInsets = WindowInsets(top = 34.dp)
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinimalModernTopAppBar(
    isAreaSelected: Boolean,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Color.White,
                            CircleShape
                        )
                        .shadow(
                            elevation = 4.dp,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.FireTruck,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = if (isAreaSelected) "Collection Status" else "SmartWaste",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        ),
                        color = Color.White
                    )
                }
            }
        },
        actions = {
            AnimatedVisibility(
                visible = isAreaSelected,
                enter = slideInHorizontally() + fadeIn(),
                exit = slideOutHorizontally() + fadeOut()
            ) {
                Surface(
                    modifier = Modifier.padding(end = 12.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White.copy(alpha = 0.25f),
                    shadowElevation = 2.dp
                ) {
                    Text(
                        text = "LIVE",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp
                        ),
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        modifier = modifier
            .height(84.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                    )
                )
            ),
        windowInsets = WindowInsets(top = 38.dp)
    )
}

@Composable
fun ModernLoadingState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "loading")
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000)
            ),
            label = "loadingRotation"
        )
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "loadingScale"
        )

        Box(
            modifier = Modifier
                .size(100.dp)
                .graphicsLayer(
                    rotationZ = rotation,
                    scaleX = scale,
                    scaleY = scale
                ),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(80.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 6.dp
            )
            Icon(
                Icons.Default.Recycling,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            message,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        LinearProgressIndicator(
            modifier = Modifier
                .width(200.dp)
                .clip(RoundedCornerShape(8.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun ModernErrorState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val shake by rememberInfiniteTransition(label = "shake").animateFloat(
            initialValue = -2f,
            targetValue = 2f,
            animationSpec = infiniteRepeatable(
                animation = tween(100),
                repeatMode = RepeatMode.Reverse
            ),
            label = "shakeAnimation"
        )

        Card(
            modifier = Modifier
                .size(120.dp)
                .graphicsLayer(translationX = shake),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            shape = CircleShape,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CloudOff,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "Oops! Something went wrong",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(20.dp)
            )
        }
    }
}

@Composable
fun ModernRouteStatusScreen(
    user: UserModel,
    routeProgressState: CommonRoutesProgressState<List<RouteProgressModel>>,
    workerFeedBackViewModel: WorkerFeedBackViewModel // Pass ViewModel
) {
    when {
        routeProgressState.isLoading -> ModernLoadingState("Loading today's route...")
        routeProgressState.error.isNotEmpty() -> ModernErrorState("Could not load route data: ${routeProgressState.error}")
        routeProgressState.succcess != null -> {
            val route = routeProgressState.succcess.find { it.routeId == user.routeId }
            if (route != null) {
                ModernRouteProgressContent(
                    user = user,
                    route = route,
                    areaProgressList = route.areaProgress,
                    workerFeedBackViewModel = workerFeedBackViewModel
                )
            } else {
                ModernEmptyState(
                    title = "No Active Route",
                    message = "There is no active collection route for your area today. Please check back later.",
                    icon = Icons.Outlined.Route
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernRouteProgressContent(
    user: UserModel,
    route: RouteProgressModel,
    areaProgressList: List<AreaProgress>,
    workerFeedBackViewModel: WorkerFeedBackViewModel
) {
    val currentAreaIndex = areaProgressList.indexOfFirst { !it.isCompleted }.takeIf { it != -1 } ?: areaProgressList.lastIndex

    var showFeedbackSheet by remember { mutableStateOf(false) }
    var feedbackGivenForThisSession by rememberSaveable { mutableStateOf(false) }
    val userArea = remember(areaProgressList, user.areaId) {
        areaProgressList.find { it.areaId == user.areaId }
    }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(userArea?.isCompleted, feedbackGivenForThisSession) {
        if (userArea?.isCompleted == true && !feedbackGivenForThisSession) {
            showFeedbackSheet = true
        }
    }

    if (showFeedbackSheet) {
        FeedbackBottomSheet(
            onDismiss = {
                showFeedbackSheet = false
                feedbackGivenForThisSession = true
            },
            onSubmit = { rating, improvement ->
                val feedbackModel = WorkerFeedBackModel(
                    feedbackId = UUID.randomUUID().toString(),
                    driverId = route.assignedDriverId,
                    collectorId = route.assignedCollectorId,
                    userId = FirebaseAuth.getInstance().currentUser!!.uid,
                    routeId = route.routeId,
                    outOf5 = rating.toString(),
                    feedbackDate = System.currentTimeMillis().toString(),
                    improvement = improvement
                )
                workerFeedBackViewModel.giveFeedBack(feedbackModel)
                coroutineScope.launch {
                    showFeedbackSheet = false
                    feedbackGivenForThisSession = true
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ModernRouteInfoHeader(user = user, route = route, areaProgressList = areaProgressList)
        }

        item {
            ModernProgressSummaryCard(areaProgressList = areaProgressList, userAreaId = user.areaId)
        }

        item {
            ModernAreaListHeader()
        }

        item {
            ModernRouteVisualization(
                areas = areaProgressList,
                currentIndex = currentAreaIndex,
                userAreaId = user.areaId
            )
        }
    }
}

@Composable
private fun ModernRouteInfoHeader(
    user: UserModel,
    route: RouteProgressModel,
    areaProgressList: List<AreaProgress>
) {
    val completedAreas = areaProgressList.count { it.isCompleted }
    val totalAreas = areaProgressList.size
    val progressPercentage = if (totalAreas > 0) completedAreas.toFloat() / totalAreas else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progressPercentage,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "progressAnimation"
    )
    val userName = FirebaseAuth.getInstance().currentUser?.displayName?.split(" ")?.firstOrNull() ?: "User"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.FrontHand,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "Hello, $userName!",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "Track your area's collection status",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(80.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        strokeWidth = 7.dp,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Complete",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Route Progress",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "$completedAreas of $totalAreas stops completed",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "${totalAreas - completedAreas} areas remaining",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )


        }
    }
}

@Composable
private fun ModernInfoCard(
    icon: ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(70.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ModernProgressSummaryCard(
    areaProgressList: List<AreaProgress>,
    userAreaId: String
) {
    val userArea = areaProgressList.find { it.areaId == userAreaId }
    val userAreaStatus = when {
        userArea?.isCompleted == true -> "Completed"
        areaProgressList.indexOfFirst { !it.isCompleted } == areaProgressList.indexOfFirst { it.areaId == userAreaId } -> "In Progress"
        else -> "Pending"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (userAreaStatus) {
                "Completed" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                "In Progress" -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val statusIcon = when (userAreaStatus) {
                "Completed" -> Icons.Default.CheckCircle
                "In Progress" -> Icons.Default.Schedule
                else -> Icons.Default.HourglassEmpty
            }

            val iconColor = when (userAreaStatus) {
                "Completed" -> MaterialTheme.colorScheme.primary
                "In Progress" -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            if (userAreaStatus == "In Progress") {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseScale"
                )

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .background(iconColor.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        statusIcon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(iconColor.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        statusIcon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Your Area Status",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    userAreaStatus,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = iconColor
                )
                if (userArea?.completedAt != null && userAreaStatus == "Completed") {
                    Text(
                        "Completed at ${formatTimestamp(userArea.completedAt, "h:mm a")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernAreaListHeader() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        MaterialTheme.colorScheme.secondary,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Sort,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                "Collection Route",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun ModernRouteVisualization(
    areas: List<AreaProgress>,
    currentIndex: Int,
    userAreaId: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp)
        ) {
            ModernEnhancedRouteMapIndicator(
                areas = areas,
                currentIndex = currentIndex,
                modifier = Modifier
                    .width(50.dp)
                    .height((areas.size * 85).dp.coerceAtMost(350.dp))
                    .padding(end = 16.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .height((areas.size * 85).dp.coerceAtMost(350.dp)),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(areas) { index, area ->
                    val isCurrent = index == currentIndex
                    val isUserArea = area.areaId == userAreaId
                    ModernViewOnlyAreaProgressItem(
                        area = area,
                        isCurrent = isCurrent,
                        isUserArea = isUserArea
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernViewOnlyAreaProgressItem(
    area: AreaProgress,
    isCurrent: Boolean,
    isUserArea: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (isUserArea) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "itemScale"
    )
    val elevation by animateDpAsState(
        targetValue = if (isUserArea) 8.dp else 3.dp,
        animationSpec = tween(300),
        label = "itemElevation"
    )

    Box(
        modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
    ) {
        ModernEnhancedAreaCard(
            area = area,
            isCurrent = isCurrent,
            isUserArea = isUserArea,
            elevation = elevation
        )
    }
}

@Composable
private fun ModernEnhancedAreaCard(
    area: AreaProgress,
    isCurrent: Boolean,
    isUserArea: Boolean,
    elevation: Dp
) {
    val cardColor by animateColorAsState(
        targetValue = when {
            area.isCompleted -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            isCurrent -> MaterialTheme.colorScheme.tertiaryContainer
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardColor"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            area.isCompleted -> MaterialTheme.colorScheme.onPrimaryContainer
            isCurrent -> MaterialTheme.colorScheme.onTertiaryContainer
            else -> MaterialTheme.colorScheme.onSurface
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardContentColor"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isUserArea) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(300),
        label = "borderColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        shape = RoundedCornerShape(16.dp),
        border = if (isUserArea) BorderStroke(2.dp, borderColor) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val statusIcon = when {
                area.isCompleted -> Icons.Default.CheckCircle
                isCurrent -> Icons.Default.Schedule
                else -> Icons.Default.HourglassEmpty
            }

            val iconColor = when {
                area.isCompleted -> MaterialTheme.colorScheme.primary
                isCurrent -> MaterialTheme.colorScheme.tertiary
                else -> contentColor.copy(alpha = 0.6f)
            }

            Icon(
                imageVector = statusIcon,
                contentDescription = "Status Icon",
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    area.areaName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = if (isUserArea) FontWeight.ExtraBold else FontWeight.Bold
                    ),
                    color = contentColor
                )

                val statusText = when {
                    area.isCompleted -> "Completed at ${formatTimestamp(area.completedAt, "h:mm a")}"
                    isCurrent -> "Collection in progress..."
                    else -> "Pending collection"
                }

                Text(
                    statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }

            if (isCurrent && !area.isCompleted) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseAlpha"
                )

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            MaterialTheme.colorScheme.tertiary.copy(alpha = alpha),
                            CircleShape
                        )
                )
            }
        }
    }
}

@Composable
private fun ModernEnhancedRouteMapIndicator(
    areas: List<AreaProgress>,
    currentIndex: Int,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val circleRadiusPx = with(density) { 12.dp.toPx() }
    val lineStrokeWidthPx = with(density) { 5.dp.toPx() }
    val markerRadiusPx = with(density) { 6.dp.toPx() }

    val completedColor = MaterialTheme.colorScheme.primary
    val pendingColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val activeColor = MaterialTheme.colorScheme.tertiary

    val animatedCurrentIndex by animateFloatAsState(
        targetValue = currentIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "currentPosition"
    )

    val pulseRadius = rememberInfiniteTransition(label = "pulseTransition").run {
        animateFloat(
            initialValue = circleRadiusPx,
            targetValue = circleRadiusPx * 1.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseRadius"
        ).value
    }

    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val itemHeight = if (areas.isNotEmpty()) size.height / areas.size else 0f
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f))

        areas.forEachIndexed { index, _ ->
            if (index < areas.size - 1) {
                val startY = itemHeight * (index + 0.5f)
                val endY = itemHeight * (index + 1.5f)
                val lineColor = when {
                    index < currentIndex -> completedColor
                    index == currentIndex -> activeColor
                    else -> pendingColor
                }

                drawLine(
                    color = lineColor,
                    start = Offset(centerX, startY + circleRadiusPx),
                    end = Offset(centerX, endY - circleRadiusPx),
                    strokeWidth = lineStrokeWidthPx,
                    pathEffect = if (index >= currentIndex) pathEffect else null
                )
            }
        }

        areas.forEachIndexed { index, area ->
            val centerY = itemHeight * (index + 0.5f)
            val isCompleted = area.isCompleted
            val isCurrent = index == currentIndex && !isCompleted

            val circleColor = when {
                isCompleted -> completedColor
                isCurrent -> activeColor
                else -> pendingColor
            }

            val radiusMultiplier = if (isCurrent) {
                1.2f + 0.15f * kotlin.math.sin(System.currentTimeMillis() / 300.0).toFloat()
            } else {
                1f
            }

            if (isCurrent) {
                drawCircle(
                    color = activeColor.copy(alpha = 0.25f),
                    radius = pulseRadius,
                    center = Offset(centerX, centerY)
                )
            }

            drawCircle(
                color = circleColor,
                radius = circleRadiusPx * radiusMultiplier,
                center = Offset(centerX, centerY),
                style = Stroke(width = lineStrokeWidthPx)
            )

            if (isCompleted) {
                drawCircle(
                    color = circleColor,
                    radius = (circleRadiusPx - lineStrokeWidthPx / 2) * radiusMultiplier,
                    center = Offset(centerX, centerY)
                )
            }
        }

        if (currentIndex < areas.size) {
            val markerY = itemHeight * (animatedCurrentIndex + 0.5f)

            drawCircle(
                color = activeColor.copy(alpha = 0.4f),
                radius = markerRadiusPx * 2f,
                center = Offset(centerX, markerY)
            )

            drawCircle(
                color = Color.White,
                radius = markerRadiusPx * 1.1f,
                center = Offset(centerX, markerY)
            )

            drawCircle(
                color = activeColor,
                radius = markerRadiusPx * 0.7f,
                center = Offset(centerX, markerY)
            )
        }
    }
}

@Composable
fun ModernEmptyState(title: String, message: String, icon: ImageVector) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
            initialValue = 0.9f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseAnimation"
        )

        Card(
            modifier = Modifier
                .size(100.dp)
                .graphicsLayer(scaleX = pulse, scaleY = pulse),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            ),
            shape = CircleShape,
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            title,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight.times(1.2f)
        )
    }
}

@Composable
fun ModernAreaSelectionScreen(
    routeProgressState: CommonRoutesProgressState<List<RouteProgressModel>>,
    onAreaSelected: (String, String, String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            routeProgressState.isLoading -> ModernLoadingState("Loading available areas...")
            routeProgressState.error.isNotEmpty() -> ModernErrorState(routeProgressState.error)
            routeProgressState.succcess != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Select Your Area",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Choose your residential area to track waste collection",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(routeProgressState.succcess) { route ->
                        ModernRouteItem(route = route, onAreaSelected = onAreaSelected)
                    }
                }
            }
        }
    }
}

@Composable
fun ModernRouteItem(
    route: RouteProgressModel,
    onAreaSelected: (String, String, String, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Default.Route,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "Route: ${route.routeId}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                route.areaProgress.forEach { area ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    area.areaName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Button(
                                onClick = {
                                    onAreaSelected(route.routeId, route.routeId, area.areaId, area.areaName)
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    "Select",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernEmailVerificationDialog(
    onDismiss: () -> Unit,
    onRefresh: () -> Unit,
    onResend: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onRefresh,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    "I've Verified",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onResend,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    "Resend Email",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Default.MarkEmailRead,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Email Not Verified",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        },
        text = {
            Text(
                "Please verify your email address to continue using the app. Check your inbox for the verification email.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

fun formatTimestamp(date: Long?, pattern: String = "yyyy-MM-dd HH:mm"): String {
    if (date == null) return "N/A"
    return try {
        SimpleDateFormat(pattern, Locale.getDefault()).format(date)
    } catch (e: Exception) {
        "Invalid Date"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleekHomeTopAppBar(
    isAreaSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val shimmerTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerTranslate by shimmerTransition.animateFloat(
        initialValue = -500f,
        targetValue = 500f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(0.3f),
            Color.White.copy(0.8f),
            Color.White.copy(0.3f),
        ),
        start = Offset(shimmerTranslate - 200f, shimmerTranslate - 200f),
        end = Offset(shimmerTranslate, shimmerTranslate)
    )

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = CircleShape,
                            spotColor = MaterialTheme.colorScheme.primary
                        )
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.5f),
                                    Color.White.copy(alpha = 0.1f)
                                )
                            ),
                            shape = CircleShape
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.FireTruck,
                        contentDescription = "Collection Truck",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }


                Column {
                    Text(
                        text = if (isAreaSelected) "Collection Status" else "SmartWaste",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color.White,
                        modifier = Modifier.animateContentSize()
                    )

                    AnimatedVisibility(
                        visible = isAreaSelected,
                        enter = fadeIn(animationSpec = tween(600, 100)) + slideInVertically { -it / 2 },
                        exit = fadeOut(animationSpec = tween(400)) + slideOutVertically { -it / 2 }
                    ) {
                        Text(
                            text = "Real-time Updates",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        actions = {

            AnimatedVisibility(
                visible = isAreaSelected,
                enter = fadeIn(animationSpec = tween(500, 200)) + scaleIn(animationSpec = spring(stiffness = Spring.StiffnessLow)),
                exit = fadeOut() + scaleOut()
            ) {
                Row(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    val pulseTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseScale by pulseTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(700),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse_scale"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .scale(pulseScale)
                            .background(color = Color(0xFF38E16A), shape = CircleShape)
                    )

                    Text(
                        text = "LIVE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White,
                        modifier = Modifier.drawBehind {
                            drawRect(shimmerBrush)
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        modifier = modifier
            .padding(bottom = 12.dp)
            .height(128.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                spotColor = Color.Black
            )
            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.2f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
            ),
        windowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackBottomSheet(
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedRating by remember { mutableIntStateOf(0) }
    var improvementText by rememberSaveable { mutableStateOf("") }


    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),

        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Collection Complete!",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                "How was the service today? Please rate the collection crew.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            StarRatingInput(
                rating = selectedRating,
                onRatingChanged = { selectedRating = it }
            )

            OutlinedTextField(
                value = improvementText,
                onValueChange = { improvementText = it },
                label = { Text("Suggestions for improvement (optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )


            Button(
                onClick = { onSubmit(selectedRating, improvementText) },
                enabled = selectedRating > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Submit Feedback",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun StarRatingInput(
    maxStars: Int = 5,
    rating: Int,
    onRatingChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        (1..maxStars).forEach { star ->
            val isSelected = star <= rating
            val iconScale by animateFloatAsState(
                targetValue = if (isSelected) 1.2f else 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ), label = "StarScale"
            )

            IconButton(onClick = { onRatingChanged(star) }) {
                Icon(
                    imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "Star $star",
                    tint = if (isSelected) Color(0xFFFFC107) else MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .size(40.dp)
                        .scale(iconScale)
                )
            }
        }
    }
}