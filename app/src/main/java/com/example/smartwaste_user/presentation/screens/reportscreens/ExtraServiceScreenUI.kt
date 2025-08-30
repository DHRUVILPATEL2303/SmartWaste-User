package com.example.smartwaste_user.presentation.screens.reportscreens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.smartwaste_user.data.models.AreaInfo
import com.example.smartwaste_user.data.models.ExtraServiceModel
import com.example.smartwaste_user.data.models.RouteModel
import com.example.smartwaste_user.presentation.viewmodels.RouteViewModel
import com.example.smartwaste_user.presentation.viewmodels.extraservicesviewmodel.ExtraServicesViewModel
import java.text.SimpleDateFormat
import java.util.*

// Dark theme colors for Extra Service Screen
object ExtraServiceDarkTheme {
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
fun ExtraServiceScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    extraServicesViewModel: ExtraServicesViewModel = hiltViewModel(),
    routeViewModel: RouteViewModel = hiltViewModel()
) {
    val getAllState by extraServicesViewModel.getAllExtraServiceState.collectAsStateWithLifecycle()
    val requestState by extraServicesViewModel.requestExtraServiceState.collectAsStateWithLifecycle()
    val deleteState by extraServicesViewModel.deleteExtraServiceState.collectAsStateWithLifecycle()
    val routesState by routeViewModel.routeState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        extraServicesViewModel.getAllExtraServices()
        routeViewModel.getAllRoutes()
    }

    LaunchedEffect(requestState, deleteState) {
        if (requestState.succcess != null) {
            Toast.makeText(context, requestState.succcess, Toast.LENGTH_SHORT).show()
            extraServicesViewModel.getAllExtraServices()
        }
        if (requestState.error.isNotEmpty()) {
            Toast.makeText(context, requestState.error, Toast.LENGTH_SHORT).show()
        }
        if (deleteState.succcess != null) {
            Toast.makeText(context, deleteState.succcess, Toast.LENGTH_SHORT).show()
            extraServicesViewModel.getAllExtraServices()
        }
        if (deleteState.error.isNotEmpty()) {
            Toast.makeText(context, deleteState.error, Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        ExtraServiceDarkTheme.background,
                        ExtraServiceDarkTheme.surface
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
                                        ExtraServiceDarkTheme.info.copy(alpha = 0.15f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Build,
                                    contentDescription = "Extra Service",
                                    tint = ExtraServiceDarkTheme.info,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    "Extra Service Requests",
                                    fontWeight = FontWeight.Bold,
                                    color = ExtraServiceDarkTheme.textPrimary,
                                    fontSize = 18.sp
                                )
                                Text(
                                    "Request additional services",
                                    fontSize = 12.sp,
                                    color = ExtraServiceDarkTheme.textSecondary
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    ExtraServiceDarkTheme.primary.copy(alpha = 0.15f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = ExtraServiceDarkTheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ExtraServiceDarkTheme.cardBackground.copy(alpha = 0.95f)
                    ),
                    actions = {
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    RequestServiceCard(
                        routes = routesState.success ?: emptyList(),
                        isLoading = requestState.isLoading,
                        onServiceRequest = { serviceModel ->
                            extraServicesViewModel.requestExtraService(serviceModel)
                        }
                    )
                }

                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Assignment,
                                contentDescription = null,
                                tint = ExtraServiceDarkTheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "Your Requests",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = ExtraServiceDarkTheme.textPrimary
                            )
                        }
                        Text(
                            "Track the status of your service requests",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ExtraServiceDarkTheme.textSecondary
                        )
                    }
                }

                if (getAllState.isLoading) {
                    item {
                        LoadingCard()
                    }
                } else if (getAllState.error.isNotEmpty()) {
                    item {
                        ErrorCard(error = getAllState.error)
                    }
                } else if (getAllState.succcess.isNullOrEmpty()) {
                    item {
                        EmptyStateCard()
                    }
                } else {
                    items(getAllState.succcess!!, key = { it.id }) { service ->
                        ServiceRequestItem(
                            service = service,
                            onDelete = { extraServicesViewModel.deleteExtraService(service.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestServiceCard(
    routes: List<RouteModel>,
    isLoading: Boolean,
    onServiceRequest: (ExtraServiceModel) -> Unit
) {
    var selectedRoute by remember { mutableStateOf<RouteModel?>(null) }
    var selectedArea by remember { mutableStateOf<AreaInfo?>(null) }
    var address by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isRouteDropdownExpanded by remember { mutableStateOf(false) }
    var isAreaDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(selectedRoute) {
        selectedArea = null
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = ExtraServiceDarkTheme.cardBackground
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, ExtraServiceDarkTheme.borderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ExtraServiceDarkTheme.primary.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        radius = 400f
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
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
                            .background(
                                ExtraServiceDarkTheme.primary.copy(alpha = 0.15f),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = ExtraServiceDarkTheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            "Request Extra Service",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = ExtraServiceDarkTheme.textPrimary
                        )
                        Text(
                            "Fill in the details for additional waste collection",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ExtraServiceDarkTheme.textSecondary
                        )
                    }
                }

                // Route Selection
                ExposedDropdownMenuBox(
                    expanded = isRouteDropdownExpanded,
                    onExpandedChange = { isRouteDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        value = selectedRoute?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = isRouteDropdownExpanded
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Route,
                                contentDescription = null,
                                tint = ExtraServiceDarkTheme.textSecondary
                            )
                        },
                        label = {
                            Text(
                                "Route",
                                color = ExtraServiceDarkTheme.textSecondary
                            )
                        },
                        placeholder = {
                            Text(
                                "Select your route",
                                color = ExtraServiceDarkTheme.textSecondary.copy(alpha = 0.7f)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ExtraServiceDarkTheme.primary,
                            unfocusedBorderColor = ExtraServiceDarkTheme.borderColor,
                            focusedTextColor = ExtraServiceDarkTheme.textPrimary,
                            unfocusedTextColor = ExtraServiceDarkTheme.textPrimary,
                            cursorColor = ExtraServiceDarkTheme.primary,
                            focusedLabelColor = ExtraServiceDarkTheme.primary,
                            unfocusedLabelColor = ExtraServiceDarkTheme.textSecondary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = isRouteDropdownExpanded,
                        onDismissRequest = { isRouteDropdownExpanded = false },
                        modifier = Modifier.background(ExtraServiceDarkTheme.cardBackground)
                    ) {
                        routes.forEach { route ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        route.name,
                                        color = ExtraServiceDarkTheme.textPrimary
                                    )
                                },
                                onClick = {
                                    selectedRoute = route
                                    isRouteDropdownExpanded = false
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = ExtraServiceDarkTheme.textPrimary
                                )
                            )
                        }
                    }
                }

                // Area Selection
                ExposedDropdownMenuBox(
                    expanded = isAreaDropdownExpanded,
                    onExpandedChange = { if (selectedRoute != null) isAreaDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        value = selectedArea?.areaName ?: "",
                        onValueChange = {},
                        readOnly = true,
                        enabled = selectedRoute != null,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = isAreaDropdownExpanded
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (selectedRoute != null)
                                    ExtraServiceDarkTheme.textSecondary
                                else
                                    ExtraServiceDarkTheme.textSecondary.copy(alpha = 0.5f)
                            )
                        },
                        label = {
                            Text(
                                "Area",
                                color = ExtraServiceDarkTheme.textSecondary
                            )
                        },
                        placeholder = {
                            Text(
                                if (selectedRoute != null) "Select your area" else "Select route first",
                                color = ExtraServiceDarkTheme.textSecondary.copy(alpha = 0.7f)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ExtraServiceDarkTheme.primary,
                            unfocusedBorderColor = ExtraServiceDarkTheme.borderColor,
                            disabledBorderColor = ExtraServiceDarkTheme.borderColor.copy(alpha = 0.5f),
                            focusedTextColor = ExtraServiceDarkTheme.textPrimary,
                            unfocusedTextColor = ExtraServiceDarkTheme.textPrimary,
                            disabledTextColor = ExtraServiceDarkTheme.textSecondary,
                            cursorColor = ExtraServiceDarkTheme.primary,
                            focusedLabelColor = ExtraServiceDarkTheme.primary,
                            unfocusedLabelColor = ExtraServiceDarkTheme.textSecondary,
                            disabledLabelColor = ExtraServiceDarkTheme.textSecondary.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = isAreaDropdownExpanded,
                        onDismissRequest = { isAreaDropdownExpanded = false },
                        modifier = Modifier.background(ExtraServiceDarkTheme.cardBackground)
                    ) {
                        selectedRoute?.areaList?.forEach { area ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        area.areaName,
                                        color = ExtraServiceDarkTheme.textPrimary
                                    )
                                },
                                onClick = {
                                    selectedArea = area
                                    isAreaDropdownExpanded = false
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = ExtraServiceDarkTheme.textPrimary
                                )
                            )
                        }
                    }
                }

                // Address Input
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            "Full Address",
                            color = ExtraServiceDarkTheme.textSecondary
                        )
                    },
                    placeholder = {
                        Text(
                            "Enter your street, house no, landmark",
                            color = ExtraServiceDarkTheme.textSecondary.copy(alpha = 0.7f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = ExtraServiceDarkTheme.textSecondary
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ExtraServiceDarkTheme.primary,
                        unfocusedBorderColor = ExtraServiceDarkTheme.borderColor,
                        focusedTextColor = ExtraServiceDarkTheme.textPrimary,
                        unfocusedTextColor = ExtraServiceDarkTheme.textPrimary,
                        cursorColor = ExtraServiceDarkTheme.primary,
                        focusedLabelColor = ExtraServiceDarkTheme.primary,
                        unfocusedLabelColor = ExtraServiceDarkTheme.textSecondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            "Description",
                            color = ExtraServiceDarkTheme.textSecondary
                        )
                    },
                    placeholder = {
                        Text(
                            "e.g., Large amount of garden waste, furniture disposal",
                            color = ExtraServiceDarkTheme.textSecondary.copy(alpha = 0.7f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            tint = ExtraServiceDarkTheme.textSecondary
                        )
                    },
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ExtraServiceDarkTheme.primary,
                        unfocusedBorderColor = ExtraServiceDarkTheme.borderColor,
                        focusedTextColor = ExtraServiceDarkTheme.textPrimary,
                        unfocusedTextColor = ExtraServiceDarkTheme.textPrimary,
                        cursorColor = ExtraServiceDarkTheme.primary,
                        focusedLabelColor = ExtraServiceDarkTheme.primary,
                        unfocusedLabelColor = ExtraServiceDarkTheme.textSecondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Submit Button
                Button(
                    onClick = {
                        val route = selectedRoute
                        val area = selectedArea
                        if (route != null && area != null && address.isNotBlank() && description.isNotBlank()) {
                            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                            val serviceModel = ExtraServiceModel(
                                address = address,
                                description = description,
                                routeId = route.id,
                                routeName = route.name,
                                areaId = area.areaId,
                                areaName = area.areaName,
                                date = currentDate
                            )
                            onServiceRequest(serviceModel)
                            address = ""
                            description = ""
                            selectedRoute = null
                            selectedArea = null
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = selectedRoute != null && selectedArea != null &&
                            address.isNotBlank() && description.isNotBlank() && !isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ExtraServiceDarkTheme.primary,
                        disabledContainerColor = ExtraServiceDarkTheme.borderColor
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    AnimatedVisibility(
                        visible = isLoading,
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
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
                                "Submitting...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = !isLoading,
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
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
                                "Submit Request",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceRequestItem(
    service: ExtraServiceModel,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = ExtraServiceDarkTheme.cardBackground
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ExtraServiceDarkTheme.borderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ExtraServiceDarkTheme.info.copy(alpha = 0.05f),
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
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    ExtraServiceDarkTheme.info.copy(alpha = 0.15f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Build,
                                contentDescription = null,
                                tint = ExtraServiceDarkTheme.info,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = service.routeName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = ExtraServiceDarkTheme.textPrimary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = ExtraServiceDarkTheme.textSecondary
                                )
                                Text(
                                    text = service.areaName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ExtraServiceDarkTheme.textSecondary
                                )
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusChip(status = service.status)
                        if (service.status.equals("Pending", ignoreCase = true)) {
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        ExtraServiceDarkTheme.error.copy(alpha = 0.1f),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Request",
                                    tint = ExtraServiceDarkTheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = ExtraServiceDarkTheme.borderColor.copy(alpha = 0.5f))

                // Details Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = ExtraServiceDarkTheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Address
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Home,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = ExtraServiceDarkTheme.textSecondary
                                )
                                Text(
                                    text = "Address:",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = ExtraServiceDarkTheme.textSecondary
                                )
                            }
                            Text(
                                text = service.address,
                                style = MaterialTheme.typography.bodyMedium,
                                color = ExtraServiceDarkTheme.textPrimary,
                                lineHeight = 20.sp
                            )
                        }

                        // Description
                        if (service.description.isNotBlank()) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Description,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = ExtraServiceDarkTheme.textSecondary
                                    )
                                    Text(
                                        text = "Description:",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = ExtraServiceDarkTheme.textSecondary
                                    )
                                }
                                Text(
                                    text = service.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ExtraServiceDarkTheme.textPrimary,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }

                // Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = ExtraServiceDarkTheme.textSecondary
                    )
                    Text(
                        text = "Requested on ${service.date}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ExtraServiceDarkTheme.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val (backgroundColor, contentColor, statusText) = when (status.lowercase()) {
        "pending" -> Triple(
            ExtraServiceDarkTheme.warning.copy(alpha = 0.15f),
            ExtraServiceDarkTheme.warning,
            "Pending"
        )
        "approved" -> Triple(
            ExtraServiceDarkTheme.info.copy(alpha = 0.15f),
            ExtraServiceDarkTheme.info,
            "Approved"
        )
        "completed" -> Triple(
            ExtraServiceDarkTheme.success.copy(alpha = 0.15f),
            ExtraServiceDarkTheme.success,
            "Completed"
        )
        "rejected" -> Triple(
            ExtraServiceDarkTheme.error.copy(alpha = 0.15f),
            ExtraServiceDarkTheme.error,
            "Rejected"
        )
        else -> Triple(
            ExtraServiceDarkTheme.surfaceVariant,
            ExtraServiceDarkTheme.textSecondary,
            status
        )
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.3f))
    ) {
        Text(
            text = statusText.uppercase(),
            color = contentColor,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun LoadingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = ExtraServiceDarkTheme.cardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, ExtraServiceDarkTheme.borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = ExtraServiceDarkTheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Text(
                "Loading your requests...",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = ExtraServiceDarkTheme.textPrimary
            )
            Text(
                "Please wait while we fetch your service requests",
                style = MaterialTheme.typography.bodyMedium,
                color = ExtraServiceDarkTheme.textSecondary,
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
            containerColor = ExtraServiceDarkTheme.cardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, ExtraServiceDarkTheme.error.copy(alpha = 0.3f))
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
                    .background(ExtraServiceDarkTheme.error.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    tint = ExtraServiceDarkTheme.error,
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(
                text = "Failed to load requests",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = ExtraServiceDarkTheme.textPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = ExtraServiceDarkTheme.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun EmptyStateCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = ExtraServiceDarkTheme.cardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, ExtraServiceDarkTheme.borderColor)
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
                    .background(ExtraServiceDarkTheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Assignment,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = ExtraServiceDarkTheme.textSecondary
                )
            }
            Text(
                text = "No requests yet",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = ExtraServiceDarkTheme.textPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "You haven't made any extra service requests. Use the form above to request additional waste collection services for your area.",
                style = MaterialTheme.typography.bodyMedium,
                color = ExtraServiceDarkTheme.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}