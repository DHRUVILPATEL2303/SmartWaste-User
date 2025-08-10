package com.example.smartwaste_user.presentation.screens.reportscreens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = if (showLocationSelection) "Select Location" else "Make Report",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            navigationIcon = {
                IconButton(onClick = {
                    if (showLocationSelection) {
                        navController.popBackStack()
                    } else {
                        showLocationSelection = true
                        selectedRoute = null
                        selectedArea = null
                        selectedRouteName = ""
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        )

        if (showLocationSelection) {
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

                        userId = FirebaseAuth.getInstance().currentUser!!.uid ,
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
            .padding(16.dp)
    ) {
        when {
            routeProgressState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading routes and areas...",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            routeProgressState.error.isNotEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = Color.Red,
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Error loading data: ${routeProgressState.error}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(onClick = onRetry) {
                            Text("Retry")
                        }
                    }
                }
            }

            routeProgressState.succcess?.isEmpty() == true -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOff,
                            contentDescription = "No Routes",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "No routes available",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Info",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Select your area to make a report",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Choose the route and specific area where you want to report an issue.",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
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
private fun RouteCard(
    route: RouteProgressModel,
    routeName: String,
    onAreaSelected: (AreaProgress) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = routeName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                StatusChip(
                    text = if (route.isRouteCompleted) "Completed" else "Active",
                    isCompleted = route.isRouteCompleted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

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

            if (route.areaProgress.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Areas (${route.areaProgress.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

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
                    Spacer(modifier = Modifier.height(8.dp))
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
        modifier = modifier
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (area.isCompleted)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (area.isCompleted) Icons.Default.CheckCircle else Icons.Default.Place,
                contentDescription = null,
                tint = if (area.isCompleted)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = area.areaName,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label:",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.width(60.dp)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Selected Location",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Route: $selectedRouteName",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Area: ${selectedArea.areaName}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Driver: ${selectedRoute.assignedDriverId.ifEmpty { "Not assigned" }}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Collector: ${selectedRoute.assignedCollectorId.ifEmpty { "Not assigned" }}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Description *",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = onDescriptionChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        label = { Text("Describe the issue in detail") },
                        maxLines = 5,
                        isError = description.isBlank()
                    )

                    if (description.isBlank()) {
                        Text(
                            text = "Description is required",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Attachments",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        TextButton(onClick = onAddAttachment) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Image",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Photo")
                        }
                    }

                    if (attachments.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            items(attachments) { attachmentUri ->
                                AttachmentItem(
                                    attachmentUriString = attachmentUri,
                                    onRemove = { onRemoveAttachment(attachmentUri) }
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onAddAttachment() },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Add Photo",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Add photos",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = isFormValid && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Submit Report",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (error.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "Error: $error",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp
                    )
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
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(8.dp))
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
                .size(24.dp)
                .background(
                    Color.Black.copy(alpha = 0.5f),
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
    }
}

@Composable
private fun StatusChip(
    text: String,
    isCompleted: Boolean
) {
    val backgroundColor = if (isCompleted)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.secondaryContainer

    val textColor = if (isCompleted)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSecondaryContainer

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}