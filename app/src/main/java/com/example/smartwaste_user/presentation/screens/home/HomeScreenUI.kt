package com.example.smartwaste_user.presentation.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.smartwaste_user.data.models.AreaInfo
import com.example.smartwaste_user.data.models.AreaProgress
import com.example.smartwaste_user.data.models.RouteModel
import com.example.smartwaste_user.data.models.RouteProgressModel
import com.example.smartwaste_user.data.models.UserModel
import com.example.smartwaste_user.data.models.WorkerFeedBackModel
import com.example.smartwaste_user.presentation.navigation.Routes
import com.example.smartwaste_user.presentation.viewmodels.CommonRoutesProgressState
import com.example.smartwaste_user.presentation.viewmodels.RouteProgressViewModel
import com.example.smartwaste_user.presentation.viewmodels.UserViewModel
import com.example.smartwaste_user.presentation.viewmodels.WorkerFeedBackViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID


object HomeDarkTheme {
    val background = Color(0xFF0D1117)
    val surface = Color(0xFF161B22)
    val surfaceVariant = Color(0xFF21262D)
    val primary = Color(0xFF2ECC71)
    val primaryVariant = Color(0xFF27AE60)
    val secondary = Color(0xFF58A6FF)
    val accent = Color(0xFFE3B341)
    val error = Color(0xFFFF6B6B)
    val success = Color(0xFF56D364)
    val warning = Color(0xFFFFA726)
    val textPrimary = Color(0xFFE6EDF3)
    val textSecondary = Color(0xFF8B949E)
    val cardBackground = Color(0xFF1C2128)
    val cardHover = Color(0xFF262C36)
    val borderColor = Color(0xFF30363D)
    val gradientStart = Color(0xFF1E40AF)
    val gradientEnd = Color(0xFF3B82F6)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenUI(
    navController: NavHostController,
    userViewModel: UserViewModel = hiltViewModel<UserViewModel>(),
    routeProgressViewModel: RouteProgressViewModel = hiltViewModel<RouteProgressViewModel>(),
    workerFeedBackViewModel: WorkerFeedBackViewModel = hiltViewModel<WorkerFeedBackViewModel>()
) {
    val userState by userViewModel.userState.collectAsState()
    val routeProgressState by routeProgressViewModel.routeProgressState.collectAsState()
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        HomeDarkTheme.background,
                        HomeDarkTheme.surface
                    )
                )
            )
    ) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    snackbar = { snackbarData ->
                        Snackbar(
                            snackbarData = snackbarData,
                            containerColor = if (snackbarData.visuals.message.contains("success", true))
                                HomeDarkTheme.success else HomeDarkTheme.error,
                            contentColor = Color.White,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(bottom = 100.dp)
                        )
                    }
                )
            },
            topBar = {
                val user = userState.succcess
                val isAreaSelected = user?.areaId?.isNotEmpty() == true

                DarkTopAppBar(
                    isAreaSelected = isAreaSelected,
                    onMapClick = {
                        user?.routeId?.let { routeId ->
                            if (routeId.isNotEmpty()) {
                                navController.navigate(Routes.RouteMapScreen(routeId = routeId))
                            }
                        }
                    }
                )
            },
            containerColor = Color.Transparent
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
                    .padding(bottom = 60.dp )
            ) {
                when {
                    userState.isLoading -> DarkLoadingState("Setting up your dashboard...")
                    userState.error.isNotEmpty() -> DarkErrorState(message = userState.error)
                    userState.succcess != null -> {
                        val user = userState.succcess!!
                        if (user.areaId.isEmpty() || user.areaName.isEmpty()) {
                            DarkAreaSelectionContent(
                                routeProgressState = routeProgressState,
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
                            DarkRouteStatusContent(
                                user = user,
                                routeProgressState = routeProgressState,
                                workerFeedBackViewModel = workerFeedBackViewModel
                            )
                        }
                    }
                }
            }

            if (showVerifyDialog && !isVerified) {
                DarkEmailVerificationDialog(
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DarkTopAppBar(
    isAreaSelected: Boolean,
    onMapClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    HomeDarkTheme.primary.copy(alpha = 0.3f),
                                    HomeDarkTheme.primary.copy(alpha = 0.1f)
                                )
                            ),
                            shape = CircleShape
                        )
                        .border(1.dp, HomeDarkTheme.primary.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Eco,
                        contentDescription = "SmartWaste",
                        tint = HomeDarkTheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = if (isAreaSelected) "Collection Status" else "SmartWaste",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.3.sp
                        ),
                        color = HomeDarkTheme.textPrimary,
                        modifier = Modifier.animateContentSize()
                    )
                    AnimatedVisibility(
                        visible = isAreaSelected,
                        enter = fadeIn(animationSpec = tween(500)) + slideInVertically { -it / 2 },
                        exit = fadeOut(animationSpec = tween(300)) + slideOutVertically { -it / 2 }
                    ) {
                        Text(
                            text = "Live tracking active",
                            style = MaterialTheme.typography.bodySmall,
                            color = HomeDarkTheme.textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedVisibility(
                    visible = isAreaSelected,
                    enter = fadeIn(animationSpec = tween(400)) + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    IconButton(
                        onClick = onMapClick,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                HomeDarkTheme.secondary.copy(alpha = 0.15f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Map,
                            contentDescription = "Open Map",
                            tint = HomeDarkTheme.secondary
                        )
                    }
                }

                AnimatedVisibility(
                    visible = isAreaSelected,
                    enter = fadeIn(animationSpec = tween(400)) + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = HomeDarkTheme.success.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, HomeDarkTheme.success.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val pulseTransition = rememberInfiniteTransition(label = "pulse")
                            val pulseScale by pulseTransition.animateFloat(
                                initialValue = 0.8f,
                                targetValue = 1.2f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulse_scale"
                            )
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .scale(pulseScale)
                                    .background(HomeDarkTheme.success, CircleShape)
                            )
                            Text(
                                text = "LIVE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = HomeDarkTheme.success,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = HomeDarkTheme.cardBackground.copy(alpha = 0.95f)
        ),
        modifier = modifier
    )
}

@Composable
fun DarkLoadingState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "loading")
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing)
            ),
            label = "rotation"
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = HomeDarkTheme.cardBackground
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = BorderStroke(1.dp, HomeDarkTheme.borderColor)
        ) {
            Column(
                modifier = Modifier.padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .graphicsLayer(rotationZ = rotation),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(80.dp),
                        color = HomeDarkTheme.primary,
                        strokeWidth = 4.dp,
                        strokeCap = StrokeCap.Round
                    )
                    Icon(
                        Icons.Default.Recycling,
                        contentDescription = null,
                        tint = HomeDarkTheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    message,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = HomeDarkTheme.textPrimary,
                    textAlign = TextAlign.Center
                )

                LinearProgressIndicator(
                    modifier = Modifier
                        .width(200.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = HomeDarkTheme.primary,
                    trackColor = HomeDarkTheme.borderColor
                )
            }
        }
    }
}

@Composable
fun DarkErrorState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = HomeDarkTheme.cardBackground
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = BorderStroke(1.dp, HomeDarkTheme.error.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(HomeDarkTheme.error.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CloudOff,
                        contentDescription = "Error",
                        tint = HomeDarkTheme.error,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Text(
                    "Something went wrong",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = HomeDarkTheme.textPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = HomeDarkTheme.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

// Convert DarkRouteStatusScreen to DarkRouteStatusContent
@Composable
fun DarkRouteStatusContent(
    user: UserModel,
    routeProgressState: CommonRoutesProgressState<List<RouteProgressModel>>,
    workerFeedBackViewModel: WorkerFeedBackViewModel
) {
    when {
        routeProgressState.isLoading -> DarkLoadingState("Loading route information...")
        routeProgressState.error.isNotEmpty() -> DarkErrorState("Unable to load route data: ${routeProgressState.error}")
        routeProgressState.succcess != null -> {
            val route = routeProgressState.succcess.find { it.routeId == user.routeId }
            if (route != null) {
                DarkRouteProgressContent(
                    user = user,
                    route = route,
                    areaProgressList = route.areaProgress,
                    workerFeedBackViewModel = workerFeedBackViewModel
                )
            } else {
                DarkEmptyState(
                    title = "No Active Collection",
                    message = "There's no scheduled waste collection for your area today. Check back tomorrow!",
                    icon = Icons.Outlined.Route
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DarkRouteProgressContent(
    user: UserModel,
    route: RouteProgressModel,
    areaProgressList: List<AreaProgress>,
    workerFeedBackViewModel: WorkerFeedBackViewModel
) {
    val currentAreaIndex = areaProgressList.indexOfFirst { !it.isCompleted }.takeIf { it != -1 } ?: areaProgressList.lastIndex
    val userArea = remember(areaProgressList, user.areaId) {
        areaProgressList.find { it.areaId == user.areaId }
    }

    var showFeedbackSheet by remember { mutableStateOf(false) }
    var feedbackGivenForThisSession by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    if (showFeedbackSheet) {
        DarkFeedbackBottomSheet(
            onDismiss = {
                showFeedbackSheet = false
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

    // Convert to Column with spacing instead of LazyColumn
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        DarkRouteInfoHeader(user = user, route = route, areaProgressList = areaProgressList)

        DarkProgressSummaryCard(
            areaProgressList = areaProgressList,
            userAreaId = user.areaId,
            showFeedbackButton = userArea?.isCompleted == true && !feedbackGivenForThisSession,
            onGiveFeedbackClick = { showFeedbackSheet = true }
        )

        DarkSectionHeader(
            title = "Collection Route",
            subtitle = "Track progress through your neighborhood"
        )

        DarkRouteVisualization(
            areas = areaProgressList,
            currentIndex = currentAreaIndex,
            userAreaId = user.areaId
        )
    }
}

@Composable
private fun DarkRouteInfoHeader(
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
        colors = CardDefaults.cardColors(
            containerColor = HomeDarkTheme.cardBackground
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, HomeDarkTheme.borderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            HomeDarkTheme.primary.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        radius = 400f
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                HomeDarkTheme.primary.copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.WavingHand,
                            contentDescription = null,
                            tint = HomeDarkTheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            "Hello, $userName!",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = HomeDarkTheme.textPrimary
                        )
                        Text(
                            "Your collection status today",
                            style = MaterialTheme.typography.bodyMedium,
                            color = HomeDarkTheme.textSecondary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.size(80.dp),
                            color = HomeDarkTheme.success,
                            trackColor = HomeDarkTheme.borderColor,
                            strokeWidth = 6.dp,
                            strokeCap = StrokeCap.Round
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${(animatedProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = HomeDarkTheme.success
                            )
                            Text(
                                text = "Done",
                                style = MaterialTheme.typography.labelSmall,
                                color = HomeDarkTheme.textSecondary
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Route Progress",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = HomeDarkTheme.textPrimary
                        )
                        Text(
                            "$completedAreas of $totalAreas areas completed",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = HomeDarkTheme.secondary
                        )
                        Text(
                            "${totalAreas - completedAreas} areas remaining",
                            style = MaterialTheme.typography.bodySmall,
                            color = HomeDarkTheme.textSecondary
                        )
                    }
                }

                HorizontalDivider(color = HomeDarkTheme.borderColor)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DarkInfoItem(
                        icon = Icons.Default.LocationOn,
                        title = "Your Area",
                        value = user.areaName.ifEmpty { "Not Set" },
                        modifier = Modifier.weight(1f)
                    )
                    DarkInfoItem(
                        icon = Icons.Default.CalendarToday,
                        title = "Collection Date",
                        value = route.date.ifEmpty { "Today" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DarkInfoItem(
    icon: ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(20.dp),
            tint = HomeDarkTheme.textSecondary
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = HomeDarkTheme.textSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = HomeDarkTheme.textPrimary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DarkProgressSummaryCard(
    areaProgressList: List<AreaProgress>,
    userAreaId: String,
    showFeedbackButton: Boolean,
    onGiveFeedbackClick: () -> Unit
) {
    val userArea = areaProgressList.find { it.areaId == userAreaId }
    val userAreaStatus = when {
        userArea?.isCompleted == true -> "Completed"
        areaProgressList.indexOfFirst { !it.isCompleted } == areaProgressList.indexOfFirst { it.areaId == userAreaId } -> "In Progress"
        else -> "Pending"
    }

    val (statusColor, statusBgColor, statusIcon) = when (userAreaStatus) {
        "Completed" -> Triple(HomeDarkTheme.success, HomeDarkTheme.success.copy(alpha = 0.15f), Icons.Default.CheckCircle)
        "In Progress" -> Triple(HomeDarkTheme.warning, HomeDarkTheme.warning.copy(alpha = 0.15f), Icons.Default.AccessTime)
        else -> Triple(HomeDarkTheme.textSecondary, HomeDarkTheme.surfaceVariant, Icons.Default.Schedule)
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = HomeDarkTheme.cardBackground
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, HomeDarkTheme.borderColor),
        modifier = Modifier.animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(statusBgColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Your Area Status",
                        style = MaterialTheme.typography.labelLarge,
                        color = HomeDarkTheme.textSecondary
                    )
                    Text(
                        userAreaStatus,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = statusColor
                    )
                    if (userArea?.completedAt != null && userAreaStatus == "Completed") {
                        Text(
                            "Completed at ${formatTimestamp(userArea.completedAt, "h:mm a")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = HomeDarkTheme.textSecondary
                        )
                    }
                }
            }

            AnimatedVisibility(visible = showFeedbackButton) {
                Button(
                    onClick = onGiveFeedbackClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HomeDarkTheme.primary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        Icons.Outlined.RateReview,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(
                        "GIVE FEEDBACK",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun DarkSectionHeader(
    title: String,
    subtitle: String? = null
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = HomeDarkTheme.textPrimary
        )
        if (subtitle != null) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = HomeDarkTheme.textSecondary
            )
        }
    }
}

@Composable
private fun DarkRouteVisualization(
    areas: List<AreaProgress>,
    currentIndex: Int,
    userAreaId: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = HomeDarkTheme.cardBackground
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, HomeDarkTheme.borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            DarkRouteMapIndicator(
                areas = areas,
                currentIndex = currentIndex,
                modifier = Modifier
                    .width(40.dp)
                    .height((areas.size * 80).dp.coerceAtMost(400.dp))
                    .padding(end = 20.dp)
            )

            // Replace LazyColumn with Column for the area items
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                areas.forEachIndexed { index, area ->
                    val isCurrent = index == currentIndex
                    val isUserArea = area.areaId == userAreaId
                    DarkAreaProgressItem(
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
private fun DarkAreaProgressItem(
    area: AreaProgress,
    isCurrent: Boolean,
    isUserArea: Boolean
) {
    val (statusColor, statusBgColor, statusIcon) = when {
        area.isCompleted -> Triple(HomeDarkTheme.success, HomeDarkTheme.success.copy(alpha = 0.15f), Icons.Default.CheckCircle)
        isCurrent -> Triple(HomeDarkTheme.warning, HomeDarkTheme.warning.copy(alpha = 0.15f), Icons.Default.AccessTime)
        else -> Triple(HomeDarkTheme.textSecondary, HomeDarkTheme.surfaceVariant, Icons.Default.Schedule)
    }

    val borderColor = if (isUserArea) HomeDarkTheme.secondary else Color.Transparent

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isUserArea) HomeDarkTheme.secondary.copy(alpha = 0.05f) else HomeDarkTheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (isUserArea) 2.dp else 0.dp,
            color = borderColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(statusBgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = "Status Icon",
                    tint = statusColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    area.areaName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = if (isUserArea) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isUserArea) HomeDarkTheme.secondary else HomeDarkTheme.textPrimary
                )

                val statusText = when {
                    area.isCompleted -> "Completed at ${formatTimestamp(area.completedAt, "h:mm a")}"
                    isCurrent -> "Collection in progress..."
                    else -> "Pending collection"
                }

                Text(
                    statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = HomeDarkTheme.textSecondary
                )
            }

            if (isUserArea) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = HomeDarkTheme.secondary
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        "YOU",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color.White,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DarkRouteMapIndicator(
    areas: List<AreaProgress>,
    currentIndex: Int,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val circleRadiusPx = with(density) { 8.dp.toPx() }
    val lineStrokeWidthPx = with(density) { 3.dp.toPx() }

    val completedColor = HomeDarkTheme.success
    val pendingColor = HomeDarkTheme.borderColor
    val activeColor = HomeDarkTheme.warning

    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val itemHeight = if (areas.isNotEmpty()) size.height / areas.size else 0f

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
                    cap = StrokeCap.Round
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

            drawCircle(
                color = circleColor,
                radius = circleRadiusPx,
                center = Offset(centerX, centerY)
            )

            if (isCompleted) {
                drawCircle(
                    color = Color.White,
                    radius = circleRadiusPx * 0.5f,
                    center = Offset(centerX, centerY)
                )
            }
        }
    }
}

@Composable
fun DarkEmptyState(title: String, message: String, icon: ImageVector) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = HomeDarkTheme.cardBackground
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = BorderStroke(1.dp, HomeDarkTheme.borderColor)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(HomeDarkTheme.surfaceVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = HomeDarkTheme.textSecondary
                    )
                }

                Text(
                    title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    color = HomeDarkTheme.textPrimary
                )

                Text(
                    message,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = HomeDarkTheme.textSecondary,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

// Convert DarkAreaSelectionScreen to DarkAreaSelectionContent
@Composable
fun DarkAreaSelectionContent(
    routeProgressState: CommonRoutesProgressState<List<RouteProgressModel>>,
    onAreaSelected: (String, String, String, String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        when {
            routeProgressState.isLoading -> DarkLoadingState("Loading available areas...")
            routeProgressState.error.isNotEmpty() -> DarkErrorState(routeProgressState.error)
            routeProgressState.succcess != null -> {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = HomeDarkTheme.cardBackground
                    ),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    border = BorderStroke(1.dp, HomeDarkTheme.borderColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(
                                    HomeDarkTheme.primary.copy(alpha = 0.15f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = HomeDarkTheme.primary,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Text(
                            "Select Your Area",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = HomeDarkTheme.textPrimary
                        )
                        Text(
                            "Choose your residential area to track waste collection status",
                            style = MaterialTheme.typography.bodyMedium,
                            color = HomeDarkTheme.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Convert LazyColumn to Column for route items
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    routeProgressState.succcess.forEach { route ->
                        DarkRouteItem(route = route, onAreaSelected = onAreaSelected)
                    }
                }
            }
        }
    }
}

@Composable
fun DarkRouteItem(
    route: RouteProgressModel,
    onAreaSelected: (String, String, String, String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = HomeDarkTheme.cardBackground
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, HomeDarkTheme.borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Route,
                    contentDescription = null,
                    tint = HomeDarkTheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    "Route ${route.routeId}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = HomeDarkTheme.textPrimary
                )
            }

            HorizontalDivider(color = HomeDarkTheme.borderColor)

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                route.areaProgress.forEach { area ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = HomeDarkTheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = HomeDarkTheme.textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = area.areaName,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = HomeDarkTheme.textPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    onAreaSelected(route.routeId, route.routeId, area.areaId, area.areaName)
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = HomeDarkTheme.primary
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    "Select",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White
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
fun DarkEmailVerificationDialog(
    onDismiss: () -> Unit,
    onRefresh: () -> Unit,
    onResend: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onRefresh,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HomeDarkTheme.primary
                )
            ) {
                Text(
                    "I've Verified",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onResend,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Resend Email",
                    style = MaterialTheme.typography.labelMedium,
                    color = HomeDarkTheme.primary
                )
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.MarkEmailRead,
                    contentDescription = null,
                    tint = HomeDarkTheme.primary
                )
                Text(
                    "Email Verification Required",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = HomeDarkTheme.textPrimary
                )
            }
        },
        text = {
            Text(
                "Please verify your email address to continue. Check your inbox for the verification email.",
                style = MaterialTheme.typography.bodyMedium,
                color = HomeDarkTheme.textSecondary
            )
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = HomeDarkTheme.cardBackground
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
fun DarkFeedbackBottomSheet(
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedRating by remember { mutableIntStateOf(0) }
    var improvementText by rememberSaveable { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = HomeDarkTheme.cardBackground,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .background(HomeDarkTheme.borderColor, RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            HomeDarkTheme.success.copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = HomeDarkTheme.success,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Text(
                    "Collection Complete!",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = HomeDarkTheme.textPrimary
                )
                Text(
                    "How was the waste collection service today?",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = HomeDarkTheme.textSecondary
                )
            }

            DarkStarRating(
                rating = selectedRating,
                onRatingChanged = { selectedRating = it }
            )

            OutlinedTextField(
                value = improvementText,
                onValueChange = { improvementText = it },
                label = { Text("Additional feedback (optional)", color = HomeDarkTheme.textSecondary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HomeDarkTheme.primary,
                    unfocusedBorderColor = HomeDarkTheme.borderColor,
                    focusedTextColor = HomeDarkTheme.textPrimary,
                    unfocusedTextColor = HomeDarkTheme.textPrimary,
                    cursorColor = HomeDarkTheme.primary,
                    focusedLabelColor = HomeDarkTheme.primary,
                    unfocusedLabelColor = HomeDarkTheme.textSecondary
                )
            )

            Button(
                onClick = { onSubmit(selectedRating, improvementText) },
                enabled = selectedRating > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HomeDarkTheme.primary,
                    disabledContainerColor = HomeDarkTheme.borderColor
                )
            ) {
                Text(
                    "Submit Feedback",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun DarkStarRating(
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
                targetValue = if (isSelected) 1.1f else 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "StarScale"
            )

            IconButton(onClick = { onRatingChanged(star) }) {
                Icon(
                    imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "Star $star",
                    tint = if (isSelected) HomeDarkTheme.accent else HomeDarkTheme.textSecondary,
                    modifier = Modifier
                        .size(32.dp)
                        .scale(iconScale)
                )
            }
        }
    }
}