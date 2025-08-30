package com.example.smartwaste_user.presentation.screens.routemap

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathEffect.Companion.dashPathEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.smartwaste_user.R
import com.example.smartwaste_user.presentation.viewmodels.RouteProgressViewModel
import com.example.smartwaste_user.presentation.viewmodels.directionviewmodel.RouteMapViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.PatternItem
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

// Dark theme colors for RouteMap
object RouteMapDarkTheme {
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
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun RouteMapScreenUI(
    navController: NavHostController,
    routeId: String,
    routeViewModel: RouteProgressViewModel = hiltViewModel(),
    mapViewModel: RouteMapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var mapType by remember { mutableStateOf(MapType.NORMAL) }
    var showInfo by remember { mutableStateOf(true) }

    val apiKey = stringResource(id = R.string.google_api_key)

    // Always use dark mode styling
    val mapStyleOptions = remember {
        MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark)
    }

    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    )

    val routeState = routeViewModel.routeProgressState.collectAsState().value
    val mapState = mapViewModel.state.collectAsState().value
    val etaAndDistanceState = mapViewModel.etaAndDistanceState.collectAsState().value
    val selectedRoute = routeState.succcess?.find { it.routeId == routeId }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(Unit) {
        routeViewModel.getallRouteProgress()
        locationPermissionsState.launchMultiplePermissionRequest()
    }

    LaunchedEffect(selectedRoute) {
        selectedRoute?.let { mapViewModel.loadRoute(it.areaProgress) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        RouteMapDarkTheme.background,
                        RouteMapDarkTheme.surface
                    )
                )
            )
    ) {
        Scaffold(
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
                                        RouteMapDarkTheme.primary.copy(alpha = 0.15f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Route,
                                    contentDescription = "Route",
                                    tint = RouteMapDarkTheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = selectedRoute?.routeId ?: "Route Map",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RouteMapDarkTheme.textPrimary
                                )
                                Text(
                                    text = "Live Tracking",
                                    fontSize = 12.sp,
                                    color = RouteMapDarkTheme.textSecondary
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
                                    RouteMapDarkTheme.surfaceVariant,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = RouteMapDarkTheme.textPrimary
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showInfo = !showInfo },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    RouteMapDarkTheme.accent.copy(alpha = 0.15f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                if (showInfo) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle Info",
                                tint = RouteMapDarkTheme.accent
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                val cameraPositionState = rememberCameraPositionState()

                LaunchedEffect(selectedRoute, mapState.polylines) {
                    val allPoints = mutableListOf<LatLng>()
                    selectedRoute?.areaProgress?.mapTo(allPoints) { LatLng(it.latitude, it.longitude) }
                    selectedRoute?.let {
                        if (it.workerLat != 0.0 || it.workerLng != 0.0) {
                            allPoints.add(LatLng(it.workerLat, it.workerLng))
                        }
                    }
                    mapState.polylines.forEach { leg ->
                        leg.mapTo(allPoints) { LatLng(it.latitude, it.longitude) }
                    }

                    if (allPoints.size > 1) {
                        val boundsBuilder = LatLngBounds.builder()
                        allPoints.forEach { boundsBuilder.include(it) }
                        cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 150))
                    } else if (allPoints.size == 1) {
                        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(allPoints.first(), 13f))
                    }
                }

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = locationPermissionsState.permissions.any { it.status.isGranted },
                        mapStyleOptions = if (mapType == MapType.NORMAL) mapStyleOptions else null,
                        mapType = mapType
                    ),
                    uiSettings = MapUiSettings(
                        myLocationButtonEnabled = false,
                        zoomControlsEnabled = false,
                        mapToolbarEnabled = false
                    )
                ) {
                    // Area markers
                    selectedRoute?.areaProgress?.forEach { area ->
                        val icon = remember(area.isCompleted) {
                            createColoredMarkerBitmapDescriptor(
                                context,
                                if (area.isCompleted) RouteMapDarkTheme.success.toArgb() else RouteMapDarkTheme.error.toArgb()
                            )
                        }
                        Marker(
                            state = MarkerState(position = LatLng(area.latitude, area.longitude)),
                            title = area.areaName,
                            snippet = if (area.isCompleted) "✅ Completed" else "⏳ Pending",
                            icon = icon
                        )
                    }

                    // Worker/truck marker
                    selectedRoute?.let { route ->
                        if (route.workerLat != 0.0 || route.workerLng != 0.0) {
                            val icon = remember {
                                createTruckMarkerBitmapDescriptor(context, RouteMapDarkTheme.primary.toArgb())
                            }
                            Marker(
                                state = MarkerState(position = LatLng(route.workerLat, route.workerLng)),
                                title = "🚛 Waste Collector",
                                snippet = "Current location of the collection truck",
                                icon = icon
                            )
                        }
                    }

                    // Route polylines
                    val polylines = if (mapState.polylines.isNotEmpty()) {
                        mapState.polylines.map { leg -> leg.map { LatLng(it.latitude, it.longitude) } }
                    } else {
                        selectedRoute?.areaProgress?.takeIf { it.size > 1 }
                            ?.let { listOf(it.map { area -> LatLng(area.latitude, area.longitude) }) }
                            ?: emptyList()
                    }

                    polylines.forEach { leg ->
                        Polyline(
                            points = leg,
                            color = RouteMapDarkTheme.primary,
                            width = 8f,

                            )
                    }
                }

                // Map controls
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = RouteMapDarkTheme.cardBackground.copy(alpha = 0.95f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    border = BorderStroke(1.dp, RouteMapDarkTheme.borderColor)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MapControlButton(
                            onClick = {
                                mapType = if (mapType == MapType.NORMAL) MapType.HYBRID else MapType.NORMAL
                            },
                            icon = if (mapType == MapType.NORMAL) Icons.Default.Satellite else Icons.Default.Map,
                            text = if (mapType == MapType.NORMAL) "Satellite" else "Normal"
                        )

                        HorizontalDivider(color = RouteMapDarkTheme.borderColor)

                        MapControlButton(
                            onClick = {
                                scope.launch {
                                    val allPoints = mutableListOf<LatLng>()
                                    selectedRoute?.areaProgress?.mapTo(allPoints) { LatLng(it.latitude, it.longitude) }
                                    selectedRoute?.let {
                                        if (it.workerLat != 0.0 || it.workerLng != 0.0) {
                                            allPoints.add(LatLng(it.workerLat, it.workerLng))
                                        }
                                    }
                                    if (allPoints.size > 1) {
                                        val boundsBuilder = LatLngBounds.builder()
                                        allPoints.forEach { boundsBuilder.include(it) }
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 150),
                                            1000
                                        )
                                    }
                                }
                            },
                            icon = Icons.Default.CenterFocusStrong,
                            text = "Fit Route"
                        )
                    }
                }

                // Location FAB
                if (locationPermissionsState.permissions.any { it.status.isGranted }) {
                    FloatingActionButton(
                        onClick = {
                            try {
                                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                    if (location != null) {
                                        val userLatLng = LatLng(location.latitude, location.longitude)
                                        scope.launch {
                                            cameraPositionState.animate(
                                                update = CameraUpdateFactory.newLatLngZoom(userLatLng, 16f),
                                                durationMs = 1000
                                            )
                                        }
                                    }
                                }
                            } catch (e: SecurityException) {
                                // Handle exception
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 16.dp),
                        containerColor = RouteMapDarkTheme.secondary,
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.MyLocation,
                            contentDescription = "My Location"
                        )
                    }
                }

                // ETA/Distance FAB
                if (selectedRoute != null && (selectedRoute.workerLat != 0.0 || selectedRoute.workerLng != 0.0)) {
                    FloatingActionButton(
                        onClick = {
                            try {
                                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                    if (location != null) {
                                        val userLatLng = LatLng(location.latitude, location.longitude)
                                        val truckLatLng = LatLng(selectedRoute.workerLat, selectedRoute.workerLng)
                                        mapViewModel.getEtaAndDistance(userLatLng, truckLatLng, apiKey)
                                    }
                                }
                            } catch (e: SecurityException) {
                                // Handle exception
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 16.dp, bottom = 16.dp),
                        containerColor = RouteMapDarkTheme.accent,
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Directions,
                            contentDescription = "Get ETA & Distance"
                        )
                    }
                }

                // Loading indicator for ETA
                AnimatedVisibility(
                    visible = etaAndDistanceState.isLoading,
                    enter = fadeIn(tween(300)),
                    exit = fadeOut(tween(300)),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = RouteMapDarkTheme.cardBackground
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                color = RouteMapDarkTheme.primary,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "Calculating route...",
                                color = RouteMapDarkTheme.textPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // ETA and Distance info
                AnimatedVisibility(
                    visible = showInfo && etaAndDistanceState.eta.isNotEmpty() && etaAndDistanceState.distance.isNotEmpty(),
                    enter = slideInVertically(tween(400)) { it } + fadeIn(tween(400)),
                    exit = slideOutVertically(tween(400)) { it } + fadeOut(tween(400))
                ) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp)
                            .fillMaxWidth(0.9f),
                        colors = CardDefaults.cardColors(
                            containerColor = RouteMapDarkTheme.cardBackground.copy(alpha = 0.95f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                        border = BorderStroke(1.dp, RouteMapDarkTheme.primary.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocalShipping,
                                    contentDescription = null,
                                    tint = RouteMapDarkTheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "Distance to Truck",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = RouteMapDarkTheme.textPrimary
                                )
                            }

                            HorizontalDivider(color = RouteMapDarkTheme.borderColor)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                InfoItem(
                                    icon = Icons.Default.Timer,
                                    label = "ETA",
                                    value = etaAndDistanceState.eta,
                                    color = RouteMapDarkTheme.success
                                )

                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(40.dp)
                                        .background(RouteMapDarkTheme.borderColor)
                                )

                                InfoItem(
                                    icon = Icons.Default.Straighten,
                                    label = "Distance",
                                    value = etaAndDistanceState.distance,
                                    color = RouteMapDarkTheme.warning
                                )
                            }
                        }
                    }
                }

                // Route progress info
                AnimatedVisibility(
                    visible = showInfo && selectedRoute != null,
                    enter = slideInVertically(tween(400)) { it } + fadeIn(tween(400)),
                    exit = slideOutVertically(tween(400)) { it } + fadeOut(tween(400))
                ) {
                    selectedRoute?.let { route ->
                        Card(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                                .fillMaxWidth(0.95f),
                            colors = CardDefaults.cardColors(
                                containerColor = RouteMapDarkTheme.cardBackground.copy(alpha = 0.95f)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                            border = BorderStroke(1.dp, RouteMapDarkTheme.borderColor)
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
                                                RouteMapDarkTheme.primary.copy(alpha = 0.15f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Route,
                                            contentDescription = null,
                                            tint = RouteMapDarkTheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = route.routeId,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = RouteMapDarkTheme.textPrimary
                                        )
                                        Text(
                                            "Collection Route",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = RouteMapDarkTheme.textSecondary
                                        )
                                    }
                                }

                                HorizontalDivider(color = RouteMapDarkTheme.borderColor)

                                val completed = route.areaProgress.count { it.isCompleted }
                                val total = route.areaProgress.size
                                val progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "Progress",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = RouteMapDarkTheme.textSecondary
                                        )
                                        Text(
                                            "$completed / $total Areas",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = RouteMapDarkTheme.textPrimary
                                        )
                                    }

                                    Text(
                                        "${(progress * 100).toInt()}%",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = RouteMapDarkTheme.success
                                    )
                                }

                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = RouteMapDarkTheme.success,
                                    trackColor = RouteMapDarkTheme.borderColor
                                )
                            }
                        }
                    }
                }

                // Loading indicator for route data
                if (routeState.isLoading) {
                    Card(
                        modifier = Modifier.align(Alignment.Center),
                        colors = CardDefaults.cardColors(
                            containerColor = RouteMapDarkTheme.cardBackground
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = RouteMapDarkTheme.primary,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                "Loading route data...",
                                color = RouteMapDarkTheme.textPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Error handling
                etaAndDistanceState.error?.let { error ->
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp)
                            .fillMaxWidth(0.9f),
                        colors = CardDefaults.cardColors(
                            containerColor = RouteMapDarkTheme.error.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, RouteMapDarkTheme.error)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = RouteMapDarkTheme.error
                            )
                            Text(
                                "Error: $error",
                                color = RouteMapDarkTheme.error,
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
private fun MapControlButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = RouteMapDarkTheme.surfaceVariant,
            contentColor = RouteMapDarkTheme.textPrimary
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun InfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = RouteMapDarkTheme.textSecondary,
            textAlign = TextAlign.Center
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = RouteMapDarkTheme.textPrimary,
            textAlign = TextAlign.Center
        )
    }
}

private fun createTruckMarkerBitmapDescriptor(context: Context, color: Int, size: Int = 128): BitmapDescriptor? {
    val truckDrawable = ContextCompat.getDrawable(context, R.drawable.ic_truck_solid) ?: return null
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val paint = Paint().apply {
        this.color = color
        isAntiAlias = true
        setShadowLayer(8f, 0f, 4f, android.graphics.Color.BLACK)
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 8f, paint)

    truckDrawable.setTint(android.graphics.Color.WHITE)
    val inset = (size * 0.25f).toInt()
    truckDrawable.setBounds(inset, inset, size - inset, size - inset)
    truckDrawable.draw(canvas)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

private fun createColoredMarkerBitmapDescriptor(context: Context, color: Int, size: Int = 100): BitmapDescriptor? {
    val markerDrawable = ContextCompat.getDrawable(context, R.drawable.red_makrer) ?: return null
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    markerDrawable.setTint(color)
    markerDrawable.setBounds(0, 0, size, size)
    markerDrawable.draw(canvas)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}