package com.example.smartwaste_user.presentation.screens.reportscreens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.smartwaste_user.data.models.AreaProgress
import com.example.smartwaste_user.data.models.ReportModel
import com.example.smartwaste_user.data.models.RouteProgressModel
import com.example.smartwaste_user.presentation.viewmodels.ReportViewModel
import com.example.smartwaste_user.presentation.viewmodels.RouteProgressViewModel
import com.example.smartwaste_user.presentation.viewmodels.RouteViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

object MakeReportDarkTheme {
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
    val info = Color(0xFF3498DB)
    val textPrimary = Color(0xFFE6EDF3)
    val textSecondary = Color(0xFF8B949E)
    val cardBackground = Color(0xFF1C2128)
    val cardHover = Color(0xFF262C36)
    val borderColor = Color(0xFF30363D)
    val inputBackground = Color(0xFF0D1117)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakeReportScreenUI(
    viewModel: ReportViewModel = hiltViewModel<ReportViewModel>(),
    routeProgressViewModel: RouteProgressViewModel = hiltViewModel<RouteProgressViewModel>(),
    routeMainViewModel: RouteViewModel = hiltViewModel<RouteViewModel>(),
    navController: NavController
) {
    val makeReportState by viewModel.makeReportState.collectAsStateWithLifecycle()
    val routeProgressState by routeProgressViewModel.routeProgressState.collectAsStateWithLifecycle()
    val routeState by routeMainViewModel.routeState.collectAsStateWithLifecycle()

    var selectedRoute by remember { mutableStateOf<RouteProgressModel?>(null) }
    var selectedRouteName by remember { mutableStateOf("") }
    var selectedArea by remember { mutableStateOf<AreaProgress?>(null) }
    var showLocationSelection by remember { mutableStateOf(true) }

    var description by remember { mutableStateOf("") }
    var attachments by remember { mutableStateOf(listOf<String>()) }
    var isFormValid by remember { mutableStateOf(false) }

    val routeNameMap = remember(routeState.success) {
        routeState.success?.associate { it.id to it.name } ?: emptyMap()
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            attachments = attachments + uris.map { it.toString() }
        }
    )

    LaunchedEffect(Unit) {
        routeProgressViewModel.getallRouteProgress()
        routeMainViewModel.getAllRoutes()
    }

    LaunchedEffect(description, selectedRoute, selectedArea) {
        isFormValid = description.isNotBlank() && selectedRoute != null && selectedArea != null
    }

    LaunchedEffect(makeReportState.success) {
        if (makeReportState.success?.isNotEmpty() == true) {
            navController.popBackStack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MakeReportDarkTheme.background,
                        MakeReportDarkTheme.surface
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (showLocationSelection)
                                            MakeReportDarkTheme.info.copy(alpha = 0.15f)
                                        else
                                            MakeReportDarkTheme.warning.copy(alpha = 0.15f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (showLocationSelection) Icons.Default.LocationOn else Icons.Default.Assignment,
                                    contentDescription = null,
                                    tint = if (showLocationSelection) MakeReportDarkTheme.info else MakeReportDarkTheme.warning,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (showLocationSelection) "Select Location" else "Make Report",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MakeReportDarkTheme.textPrimary
                                )
                                Text(
                                    text = if (showLocationSelection) "Choose your area" else "Describe the issue",
                                    fontSize = 12.sp,
                                    color = MakeReportDarkTheme.textSecondary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MakeReportDarkTheme.cardBackground.copy(alpha = 0.95f)
                    ),
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                    navController.popBackStack()

                            },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    MakeReportDarkTheme.primary.copy(alpha = 0.15f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MakeReportDarkTheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    actions = {
                        // Progress indicator
                        if (!showLocationSelection) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MakeReportDarkTheme.success.copy(alpha = 0.15f)
                                ),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, MakeReportDarkTheme.success.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "STEP 2/2",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = MakeReportDarkTheme.success,
                                        letterSpacing = 0.8.sp
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                )
            }
        ) { paddingValues ->
            AnimatedContent(
                targetState = showLocationSelection,
                transitionSpec = {
                    slideInHorizontally(
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        initialOffsetX = { if (targetState) -it else it }
                    ) + fadeIn() togetherWith slideOutHorizontally(
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        targetOffsetX = { if (targetState) it else -it }
                    ) + fadeOut()
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                label = "screen_transition"
            ) { showLocation ->
                if (showLocation) {
                    LocationSelectionContent(
                        routeProgressState = routeProgressState,
                        routeNameMap = routeNameMap,
                        onLocationSelected = { route, area ->
                            selectedRoute = route
                            selectedArea = area
                            selectedRouteName = routeNameMap[route.routeId] ?: "Route ${route.routeId}"
                            showLocationSelection = false
                        },
                        onRetry = { routeProgressViewModel.getallRouteProgress() }
                    )
                } else {
                    ReportFormContent(
                        selectedRoute = selectedRoute!!,
                        selectedRouteName = selectedRouteName,
                        selectedArea = selectedArea!!,
                        description = description,
                        onDescriptionChange = { description = it },
                        attachments = attachments,
                        onAddAttachment = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onRemoveAttachment = { attachmentUri ->
                            attachments = attachments.filter { it != attachmentUri }
                        },
                        isFormValid = isFormValid,
                        isLoading = makeReportState.isLoading,
                        error = makeReportState.error,
                        onSubmit = {
                            val report = ReportModel(
                                userId = FirebaseAuth.getInstance().currentUser!!.uid,
                                againstDriverId = selectedRoute!!.assignedDriverId,
                                againstCollectorId = selectedRoute!!.assignedCollectorId,
                                routeId = selectedRoute!!.routeId,
                                areaId = selectedArea!!.areaId,
                                areaName = selectedArea!!.areaName,
                                description = description,
                                attachments = attachments,
                                status = "Pending",
                                reportDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                            )
                            viewModel.makeReport(report)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationSelectionContent(
    routeProgressState: com.example.smartwaste_user.presentation.viewmodels.CommonRoutesProgressState<List<RouteProgressModel>>,
    routeNameMap: Map<String, String>,
    onLocationSelected: (RouteProgressModel, AreaProgress) -> Unit,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        when {
            routeProgressState.isLoading -> {
                LoadingStateCard()
            }

            routeProgressState.error.isNotEmpty() -> {
                ErrorStateCard(
                    error = routeProgressState.error,
                    onRetry = onRetry
                )
            }

            routeProgressState.succcess?.isEmpty() == true -> {
                EmptyStateCard()
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        InstructionCard()
                    }

                    items(routeProgressState.succcess ?: emptyList()) { route ->
                        val routeName = routeNameMap[route.routeId] ?: "Route ${route.routeId}"
                        RouteCard(
                            route = route,
                            routeName = routeName,
                            onAreaSelected = { area ->
                                onLocationSelected(route, area)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InstructionCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MakeReportDarkTheme.cardBackground
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MakeReportDarkTheme.info.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MakeReportDarkTheme.info.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        radius = 300f
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                MakeReportDarkTheme.info.copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = MakeReportDarkTheme.info,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Select your area to make a report",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MakeReportDarkTheme.textPrimary
                        )
                        Text(
                            text = "Choose the route and specific area where you want to report an issue",
                            fontSize = 14.sp,
                            color = MakeReportDarkTheme.textSecondary,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteCard(
    route: RouteProgressModel,
    routeName: String,
    onAreaSelected: (AreaProgress) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MakeReportDarkTheme.cardBackground
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MakeReportDarkTheme.borderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MakeReportDarkTheme.primary.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        radius = 300f
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Route,
                            contentDescription = null,
                            tint = MakeReportDarkTheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = routeName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MakeReportDarkTheme.textPrimary
                        )
                    }

                    StatusChip(
                        text = if (route.isRouteCompleted) "Completed" else "Active",
                        isCompleted = route.isRouteCompleted
                    )
                }

                HorizontalDivider(color = MakeReportDarkTheme.borderColor.copy(alpha = 0.5f))

                // Route Details
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RouteDetailRow(
                        icon = Icons.Default.Person,
                        label = "Driver",
                        value = route.assignedDriverId.ifEmpty { "Not assigned" }
                    )

                    RouteDetailRow(
                        icon = Icons.Default.Group,
                        label = "Collector",
                        value = route.assignedCollectorId.ifEmpty { "Not assigned" }
                    )

                    RouteDetailRow(
                        icon = Icons.Default.LocalShipping,
                        label = "Truck",
                        value = route.assignedTruckId.ifEmpty { "Not assigned" }
                    )
                }

                // Areas Section
                if (route.areaProgress.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MakeReportDarkTheme.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Areas (${route.areaProgress.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MakeReportDarkTheme.textSecondary
                            )
                        }

                        // Areas Grid
                        route.areaProgress.chunked(2).forEach { rowAreas ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowAreas.forEach { area ->
                                    AreaButton(
                                        area = area,
                                        modifier = Modifier.weight(1f),
                                        onClick = { onAreaSelected(area) }
                                    )
                                }
                                if (rowAreas.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AreaButton(
    area: AreaProgress,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (area.isCompleted)
                MakeReportDarkTheme.success.copy(alpha = 0.15f)
            else
                MakeReportDarkTheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (area.isCompleted) MakeReportDarkTheme.success.copy(alpha = 0.3f) else MakeReportDarkTheme.borderColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (area.isCompleted) Icons.Default.CheckCircle else Icons.Default.Place,
                contentDescription = null,
                tint = if (area.isCompleted)
                    MakeReportDarkTheme.success
                else
                    MakeReportDarkTheme.textSecondary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = area.areaName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MakeReportDarkTheme.textPrimary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun RouteDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MakeReportDarkTheme.secondary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "$label:",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MakeReportDarkTheme.textSecondary,
            modifier = Modifier.width(70.dp)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MakeReportDarkTheme.textPrimary
        )
    }
}

@Composable
private fun ReportFormContent(
    selectedRoute: RouteProgressModel,
    selectedRouteName: String,
    selectedArea: AreaProgress,
    description: String,
    onDescriptionChange: (String) -> Unit,
    attachments: List<String>,
    onAddAttachment: () -> Unit,
    onRemoveAttachment: (String) -> Unit,
    isFormValid: Boolean,
    isLoading: Boolean,
    error: String,
    onSubmit: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            SelectedLocationCard(
                selectedRoute = selectedRoute,
                selectedRouteName = selectedRouteName,
                selectedArea = selectedArea
            )
        }

        item {
            DescriptionCard(
                description = description,
                onDescriptionChange = onDescriptionChange
            )
        }

        item {
            AttachmentsCard(
                attachments = attachments,
                onAddAttachment = onAddAttachment,
                onRemoveAttachment = onRemoveAttachment
            )
        }

        item {
            SubmitButton(
                isFormValid = isFormValid,
                isLoading = isLoading,
                onSubmit = onSubmit
            )
        }

        if (error.isNotEmpty()) {
            item {
                ErrorCard(error = error)
            }
        }
    }
}

@Composable
private fun SelectedLocationCard(
    selectedRoute: RouteProgressModel,
    selectedRouteName: String,
    selectedArea: AreaProgress
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MakeReportDarkTheme.cardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MakeReportDarkTheme.success.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MakeReportDarkTheme.success.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        radius = 300f
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                MakeReportDarkTheme.success.copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = MakeReportDarkTheme.success,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Selected Location",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MakeReportDarkTheme.textPrimary
                        )
                        Text(
                            text = "Report will be filed for this location",
                            fontSize = 14.sp,
                            color = MakeReportDarkTheme.textSecondary
                        )
                    }
                }

                HorizontalDivider(color = MakeReportDarkTheme.borderColor.copy(alpha = 0.5f))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LocationDetailRow(
                        icon = Icons.Default.Route,
                        label = "Route",
                        value = selectedRouteName
                    )
                    LocationDetailRow(
                        icon = Icons.Default.LocationOn,
                        label = "Area",
                        value = selectedArea.areaName
                    )
                    LocationDetailRow(
                        icon = Icons.Default.Person,
                        label = "Driver",
                        value = selectedRoute.assignedDriverId.ifEmpty { "Not assigned" }
                    )
                    LocationDetailRow(
                        icon = Icons.Default.Group,
                        label = "Collector",
                        value = selectedRoute.assignedCollectorId.ifEmpty { "Not assigned" }
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MakeReportDarkTheme.success,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "$label:",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MakeReportDarkTheme.textSecondary,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MakeReportDarkTheme.textPrimary
        )
    }
}

@Composable
private fun DescriptionCard(
    description: String,
    onDescriptionChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MakeReportDarkTheme.cardBackground
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MakeReportDarkTheme.borderColor)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MakeReportDarkTheme.warning.copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        tint = MakeReportDarkTheme.warning,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Description *",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MakeReportDarkTheme.textPrimary
                    )
                    Text(
                        text = "Describe the issue in detail",
                        fontSize = 14.sp,
                        color = MakeReportDarkTheme.textSecondary
                    )
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                label = {
                    Text(
                        "What happened? Be specific about the issue...",
                        color = MakeReportDarkTheme.textSecondary
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MakeReportDarkTheme.primary,
                    unfocusedBorderColor = MakeReportDarkTheme.borderColor,
                    errorBorderColor = MakeReportDarkTheme.error,
                    focusedTextColor = MakeReportDarkTheme.textPrimary,
                    unfocusedTextColor = MakeReportDarkTheme.textPrimary,
                    cursorColor = MakeReportDarkTheme.primary,
                    focusedLabelColor = MakeReportDarkTheme.primary,
                    unfocusedLabelColor = MakeReportDarkTheme.textSecondary,
                    errorLabelColor = MakeReportDarkTheme.error
                ),
                shape = RoundedCornerShape(12.dp),
                maxLines = 6,
                isError = description.isBlank()
            )

            AnimatedVisibility(
                visible = description.isBlank(),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = MakeReportDarkTheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Description is required",
                        color = MakeReportDarkTheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun AttachmentsCard(
    attachments: List<String>,
    onAddAttachment: () -> Unit,
    onRemoveAttachment: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MakeReportDarkTheme.cardBackground
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MakeReportDarkTheme.borderColor)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                MakeReportDarkTheme.info.copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = MakeReportDarkTheme.info,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Attachments (${attachments.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MakeReportDarkTheme.textPrimary
                        )
                        Text(
                            text = "Add photos to support your report",
                            fontSize = 14.sp,
                            color = MakeReportDarkTheme.textSecondary
                        )
                    }
                }

                Button(
                    onClick = onAddAttachment,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MakeReportDarkTheme.info
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Photo",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Add Photo",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (attachments.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(attachments) { attachmentUri ->
                        AttachmentItem(
                            attachmentUriString = attachmentUri,
                            onRemove = { onRemoveAttachment(attachmentUri) }
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clickable { onAddAttachment() },
                    colors = CardDefaults.cardColors(
                        containerColor = MakeReportDarkTheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                       width =  2.dp,
                        color = Color.White
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Add Photo",
                                tint = MakeReportDarkTheme.info,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "Tap to add photos",
                                color = MakeReportDarkTheme.info,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttachmentItem(
    attachmentUriString: String,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.size(100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MakeReportDarkTheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, MakeReportDarkTheme.borderColor)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            AsyncImage(
                model = Uri.parse(attachmentUriString),
                contentDescription = "Attachment",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Remove button
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(28.dp)
                    .background(
                        MakeReportDarkTheme.error.copy(alpha = 0.9f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Overlay for better visibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.2f)
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun SubmitButton(
    isFormValid: Boolean,
    isLoading: Boolean,
    onSubmit: () -> Unit
) {
    Button(
        onClick = onSubmit,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = isFormValid && !isLoading,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MakeReportDarkTheme.primary,
            disabledContainerColor = MakeReportDarkTheme.borderColor
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        AnimatedContent(
            targetState = isLoading,
            label = "submit_button_content"
        ) { loading ->
            if (loading) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Submitting Report...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                    Text(
                        text = "Submit Report",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(
    text: String,
    isCompleted: Boolean
) {
    val (backgroundColor, textColor) = if (isCompleted) {
        MakeReportDarkTheme.success.copy(alpha = 0.15f) to MakeReportDarkTheme.success
    } else {
        MakeReportDarkTheme.warning.copy(alpha = 0.15f) to MakeReportDarkTheme.warning
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.3f))
    ) {
        Text(
            text = text.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

// State Cards
@Composable
private fun LoadingStateCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MakeReportDarkTheme.cardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MakeReportDarkTheme.borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = MakeReportDarkTheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Text(
                "Loading routes and areas...",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MakeReportDarkTheme.textPrimary
            )
            Text(
                "Please wait while we fetch available locations",
                fontSize = 14.sp,
                color = MakeReportDarkTheme.textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorStateCard(error: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MakeReportDarkTheme.cardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MakeReportDarkTheme.error.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MakeReportDarkTheme.error.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Error",
                    tint = MakeReportDarkTheme.error,
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(
                text = "Failed to load data",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MakeReportDarkTheme.textPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = error,
                fontSize = 14.sp,
                color = MakeReportDarkTheme.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MakeReportDarkTheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Try Again",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun EmptyStateCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MakeReportDarkTheme.cardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MakeReportDarkTheme.borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(MakeReportDarkTheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOff,
                    contentDescription = "No Routes",
                    tint = MakeReportDarkTheme.textSecondary,
                    modifier = Modifier.size(40.dp)
                )
            }
            Text(
                text = "No routes available",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MakeReportDarkTheme.textPrimary
            )
            Text(
                text = "There are currently no active routes in the system",
                fontSize = 14.sp,
                color = MakeReportDarkTheme.textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorCard(error: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MakeReportDarkTheme.cardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MakeReportDarkTheme.error.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = MakeReportDarkTheme.error,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Error: $error",
                color = MakeReportDarkTheme.error,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}