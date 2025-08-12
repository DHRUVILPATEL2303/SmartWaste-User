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
import androidx.compose.ui.graphics.StrokeCap
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
            CleanTopAppBar(isAreaSelected = userState.succcess?.areaId?.isNotEmpty() == true)
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                userState.isLoading -> CleanLoadingState("Setting up your dashboard...")
                userState.error.isNotEmpty() -> CleanErrorState(message = userState.error)
                userState.succcess != null -> {
                    val user = userState.succcess!!
                    if (user.areaId.isEmpty() || user.areaName.isEmpty()) {
                        CleanAreaSelectionScreen(
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
                        CleanRouteStatusScreen(
                            user = user,
                            routeProgressState = routeProgressViewModel.routeProgressState.collectAsState().value,
                            workerFeedBackViewModel = workerFeedBackViewModel
                        )
                    }
                }
            }
        }

        if (showVerifyDialog && !isVerified) {
            CleanEmailVerificationDialog(
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
fun CleanTopAppBar(
    isAreaSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val shimmerTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerTranslate by shimmerTransition.animateFloat(
        initialValue = -300f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            Color.White.copy(alpha = 0.25f),
                            CircleShape
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.FireTruck,
                        contentDescription = "SmartWaste",
                        tint = Color.White,
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
                        color = Color.White,
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
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        actions = {
            AnimatedVisibility(
                visible = isAreaSelected,
                enter = fadeIn(animationSpec = tween(400)) + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Surface(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .height(28.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
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
                                .background(Color(0xFF10B981), CircleShape)
                        )

                        Text(
                            text = "LIVE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        modifier = modifier
            .height(100.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E40AF),
                        Color(0xFF3B82F6)
                    )
                ),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .padding(horizontal = 20.dp),
        windowInsets = WindowInsets.statusBars
    )
}

@Composable
fun CleanLoadingState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
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

        Box(
            modifier = Modifier
                .size(80.dp)
                .graphicsLayer(rotationZ = rotation),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(80.dp),
                color = Color(0xFF3B82F6),
                strokeWidth = 4.dp,
                strokeCap = StrokeCap.Round
            )
            Icon(
                Icons.Default.Recycling,
                contentDescription = null,
                tint = Color(0xFF3B82F6),
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            message,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = Color(0xFF374151),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        LinearProgressIndicator(
            modifier = Modifier
                .width(200.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = Color(0xFF3B82F6),
            trackColor = Color(0xFFE5E7EB)
        )
    }
}

@Composable
fun CleanErrorState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color(0xFFFEE2E2), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CloudOff,
                contentDescription = "Error",
                tint = Color(0xFFDC2626),
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Something went wrong",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color(0xFF111827),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Composable
fun CleanRouteStatusScreen(
    user: UserModel,
    routeProgressState: CommonRoutesProgressState<List<RouteProgressModel>>,
    workerFeedBackViewModel: WorkerFeedBackViewModel
) {
    when {
        routeProgressState.isLoading -> CleanLoadingState("Loading route information...")
        routeProgressState.error.isNotEmpty() -> CleanErrorState("Unable to load route data: ${routeProgressState.error}")
        routeProgressState.succcess != null -> {
            val route = routeProgressState.succcess.find { it.routeId == user.routeId }
            if (route != null) {
                CleanRouteProgressContent(
                    user = user,
                    route = route,
                    areaProgressList = route.areaProgress,
                    workerFeedBackViewModel = workerFeedBackViewModel
                )
            } else {
                CleanEmptyState(
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
private fun CleanRouteProgressContent(
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
        CleanFeedbackBottomSheet(
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CleanRouteInfoHeader(user = user, route = route, areaProgressList = areaProgressList)
        }

        item {
            CleanProgressSummaryCard(
                areaProgressList = areaProgressList,
                userAreaId = user.areaId,
                showFeedbackButton = userArea?.isCompleted == true && !feedbackGivenForThisSession,
                onGiveFeedbackClick = { showFeedbackSheet = true }
            )
        }

        item {
            CleanSectionHeader(
                title = "Collection Route",
                subtitle = "Track progress through your neighborhood"
            )
        }

        item {
            CleanRouteVisualization(
                areas = areaProgressList,
                currentIndex = currentAreaIndex,
                userAreaId = user.areaId
            )
        }
    }
}

@Composable
private fun CleanRouteInfoHeader(
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(24.dp))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF3B82F6).copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.WavingHand,
                    contentDescription = null,
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(24.dp)
                )
            }

            Column {
                Text(
                    "Hello, $userName!",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF111827)
                )
                Text(
                    "Your collection status today",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7280)
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
                    color = Color(0xFF10B981),
                    trackColor = Color(0xFFE5E7EB),
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
                        color = Color(0xFF10B981)
                    )
                    Text(
                        text = "Done",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Route Progress",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF111827)
                )
                Text(
                    "$completedAreas of $totalAreas areas completed",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color(0xFF3B82F6)
                )
                Text(
                    "${totalAreas - completedAreas} areas remaining",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280)
                )
            }
        }

        HorizontalDivider(color = Color(0xFFF3F4F6))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CleanInfoItem(
                icon = Icons.Default.LocationOn,
                title = "Your Area",
                value = user.areaName.ifEmpty { "Not Set" },
                modifier = Modifier.weight(1f)
            )
            CleanInfoItem(
                icon = Icons.Default.CalendarToday,
                title = "Collection Date",
                value = route.date.ifEmpty { "Today" },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CleanInfoItem(
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
            tint = Color(0xFF6B7280)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF6B7280)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = Color(0xFF111827),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CleanProgressSummaryCard(
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
        "Completed" -> Triple(Color(0xFF10B981), Color(0xFFECFDF5), Icons.Default.CheckCircle)
        "In Progress" -> Triple(Color(0xFFF59E0B), Color(0xFFFEF3C7), Icons.Default.AccessTime)
        else -> Triple(Color(0xFF6B7280), Color(0xFFF3F4F6), Icons.Default.Schedule)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(20.dp))
            .padding(20.dp)
            .animateContentSize(),
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
                    color = Color(0xFF6B7280)
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
                        color = Color(0xFF6B7280)
                    )
                }
            }
        }

        AnimatedVisibility(visible = showFeedbackButton) {
            OutlinedButton(
                onClick = onGiveFeedbackClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF3B82F6)
                ),
                border = BorderStroke(1.dp, Color(0xFF3B82F6))
            ) {
                Icon(
                    Icons.Outlined.RateReview,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(
                    "GIVE FEEDBACK",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
private fun CleanSectionHeader(
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
            color = Color(0xFF111827)
        )
        if (subtitle != null) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6B7280)
            )
        }
    }
}

@Composable
private fun CleanRouteVisualization(
    areas: List<AreaProgress>,
    currentIndex: Int,
    userAreaId: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        CleanRouteMapIndicator(
            areas = areas,
            currentIndex = currentIndex,
            modifier = Modifier
                .width(40.dp)
                .height((areas.size * 80).dp.coerceAtMost(400.dp))
                .padding(end = 20.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .height((areas.size * 80).dp.coerceAtMost(400.dp)),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(areas) { index, area ->
                val isCurrent = index == currentIndex
                val isUserArea = area.areaId == userAreaId
                CleanAreaProgressItem(
                    area = area,
                    isCurrent = isCurrent,
                    isUserArea = isUserArea
                )
            }
        }
    }
}

@Composable
private fun CleanAreaProgressItem(
    area: AreaProgress,
    isCurrent: Boolean,
    isUserArea: Boolean
) {
    val (statusColor, statusBgColor, statusIcon) = when {
        area.isCompleted -> Triple(Color(0xFF10B981), Color(0xFFECFDF5), Icons.Default.CheckCircle)
        isCurrent -> Triple(Color(0xFFF59E0B), Color(0xFFFEF3C7), Icons.Default.AccessTime)
        else -> Triple(Color(0xFF6B7280), Color(0xFFF3F4F6), Icons.Default.Schedule)
    }

    val borderColor = if (isUserArea) Color(0xFF3B82F6) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isUserArea) Color(0xFF3B82F6).copy(alpha = 0.05f) else Color(0xFFF9FAFB),
                RoundedCornerShape(16.dp)
            )
            .border(
                width = if (isUserArea) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
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
                color = if (isUserArea) Color(0xFF1E40AF) else Color(0xFF111827)
            )

            val statusText = when {
                area.isCompleted -> "Completed at ${formatTimestamp(area.completedAt, "h:mm a")}"
                isCurrent -> "Collection in progress..."
                else -> "Pending collection"
            }

            Text(
                statusText,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B7280)
            )
        }

        if (isUserArea) {
            Box(
                modifier = Modifier
                    .background(Color(0xFF3B82F6), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    "YOU",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color.White,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun CleanRouteMapIndicator(
    areas: List<AreaProgress>,
    currentIndex: Int,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val circleRadiusPx = with(density) { 8.dp.toPx() }
    val lineStrokeWidthPx = with(density) { 3.dp.toPx() }

    val completedColor = Color(0xFF10B981)
    val pendingColor = Color(0xFFE5E7EB)
    val activeColor = Color(0xFFF59E0B)

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
fun CleanEmptyState(title: String, message: String, icon: ImageVector) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color(0xFFF3F4F6), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = Color(0xFF9CA3AF)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            title,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center,
            color = Color(0xFF111827)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color(0xFF6B7280),
            lineHeight = 24.sp
        )
    }
}

@Composable
fun CleanAreaSelectionScreen(
    routeProgressState: CommonRoutesProgressState<List<RouteProgressModel>>,
    onAreaSelected: (String, String, String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            routeProgressState.isLoading -> CleanLoadingState("Loading available areas...")
            routeProgressState.error.isNotEmpty() -> CleanErrorState(routeProgressState.error)
            routeProgressState.succcess != null -> {
                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(24.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color(0xFF3B82F6).copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Text(
                        "Select Your Area",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF111827)
                    )
                    Text(
                        "Choose your residential area to track waste collection status",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(routeProgressState.succcess) { route ->
                        CleanRouteItem(route = route, onAreaSelected = onAreaSelected)
                    }
                }
            }
        }
    }
}

@Composable
fun CleanRouteItem(
    route: RouteProgressModel,
    onAreaSelected: (String, String, String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(20.dp))
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
                tint = Color(0xFF3B82F6),
                modifier = Modifier.size(24.dp)
            )
            Text(
                "Route ${route.routeId}",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF111827)
            )
        }

        HorizontalDivider(color = Color(0xFFF3F4F6))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            route.areaProgress.forEach { area ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            area.areaName,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFF111827)
                        )
                    }

                    Button(
                        onClick = {
                            onAreaSelected(route.routeId, route.routeId, area.areaId, area.areaName)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B82F6)
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

@Composable
fun CleanEmailVerificationDialog(
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
                    containerColor = Color(0xFF3B82F6)
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
                    color = Color(0xFF3B82F6)
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
                    tint = Color(0xFF3B82F6)
                )
                Text(
                    "Email Verification Required",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        },
        text = {
            Text(
                "Please verify your email address to continue. Check your inbox for the verification email.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6B7280)
            )
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
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
fun CleanFeedbackBottomSheet(
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedRating by remember { mutableIntStateOf(0) }
    var improvementText by rememberSaveable { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .size(width = 32.dp, height = 4.dp)
                    .background(Color(0xFFE5E7EB), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color(0xFF10B981).copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(30.dp)
                    )
                }

                Text(
                    "Collection Complete!",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF111827)
                )

                Text(
                    "How was the waste collection service today?",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF6B7280)
                )
            }

            // Rating
            CleanStarRating(
                rating = selectedRating,
                onRatingChanged = { selectedRating = it }
            )

            // Feedback Input
            OutlinedTextField(
                value = improvementText,
                onValueChange = { improvementText = it },
                label = { Text("Additional feedback (optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3B82F6),
                    focusedLabelColor = Color(0xFF3B82F6)
                )
            )

            // Submit Button
            Button(
                onClick = { onSubmit(selectedRating, improvementText) },
                enabled = selectedRating > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3B82F6),
                    disabledContainerColor = Color(0xFFE5E7EB)
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
fun CleanStarRating(
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
                    tint = if (isSelected) Color(0xFFFBBF24) else Color(0xFF9CA3AF),
                    modifier = Modifier
                        .size(32.dp)
                        .scale(iconScale)
                )
            }
        }
    }
}