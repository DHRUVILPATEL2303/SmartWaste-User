package com.example.smartwaste_user.presentation.screens.routemap

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

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

    var isDarkMode by rememberSaveable { mutableStateOf(false) }
    var mapType by remember { mutableStateOf(MapType.NORMAL) }

    val backgroundColor = if (isDarkMode) Color(0xFF121212) else MaterialTheme.colorScheme.background
    val surfaceColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val onSurfaceColor = if (isDarkMode) Color.White else Color.Black
    val primaryColor = if (isDarkMode) Color(0xFF90CAF9) else MaterialTheme.colorScheme.primary
    val cardColor = if (isDarkMode) Color(0xFF2D2D2D) else Color.White

    val apiKey = stringResource(id = R.string.google_api_key)

    val mapStyleOptions = if (isDarkMode && mapType == MapType.NORMAL) {
        remember { MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark) }
    } else {
        null
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedRoute?.routeId ?: "Route Map", color = onSurfaceColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = onSurfaceColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        },
        containerColor = backgroundColor,
        contentColor = onSurfaceColor
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(backgroundColor)
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 180.dp),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = locationPermissionsState.permissions.any { it.status.isGranted },
                    mapStyleOptions = mapStyleOptions,
                    mapType = mapType
                ),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = false,
                    zoomControlsEnabled = false
                )
            ) {
                selectedRoute?.areaProgress?.forEach { area ->
                    val icon = remember(area.isCompleted) {
                        createColoredMarkerBitmapDescriptor(
                            context,
                            if (area.isCompleted) android.graphics.Color.GREEN else android.graphics.Color.RED
                        )
                    }
                    Marker(
                        state = MarkerState(position = LatLng(area.latitude, area.longitude)),
                        title = area.areaName,
                        icon = icon
                    )
                }

                selectedRoute?.let { route ->
                    if (route.workerLat != 0.0 || route.workerLng != 0.0) {
                        val icon = remember(isDarkMode) {
                            createTruckMarkerBitmapDescriptor(context, primaryColor.toArgb())
                        }
                        Marker(
                            state = MarkerState(position = LatLng(route.workerLat, route.workerLng)),
                            title = "Waste Collector",
                            snippet = "Current location of the collection truck.",
                            icon = icon
                        )
                    }
                }

                val polylines = if (mapState.polylines.isNotEmpty()) {
                    mapState.polylines.map { leg -> leg.map { LatLng(it.latitude, it.longitude) } }
                } else {
                    selectedRoute?.areaProgress?.takeIf { it.size > 1 }
                        ?.let { listOf(it.map { area -> LatLng(area.latitude, area.longitude) }) }
                        ?: emptyList()
                }

                polylines.forEach { leg ->
                    Polyline(points = leg, color = Color(0xFF388E3C), width = 12f)
                }
            }

            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.95f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MapControlButton(
                        onClick = {
                            mapType = if (mapType == MapType.NORMAL) MapType.HYBRID else MapType.NORMAL
                        },
                        icon = if (mapType == MapType.NORMAL) Icons.Default.Satellite else Icons.Default.Map,
                        text = if (mapType == MapType.NORMAL) "Satellite" else "Normal",
                        isDarkMode = isDarkMode,
                        primaryColor = primaryColor,
                        onSurfaceColor = onSurfaceColor
                    )

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(onSurfaceColor.copy(alpha = 0.3f))
                    )

                    MapControlButton(
                        onClick = { isDarkMode = !isDarkMode },
                        icon = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        text = if (isDarkMode) "Light" else "Dark",
                        isDarkMode = isDarkMode,
                        primaryColor = primaryColor,
                        onSurfaceColor = onSurfaceColor
                    )
                }
            }

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
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 200.dp),
                    containerColor = primaryColor
                ) {
                    Icon(
                        Icons.Default.MyLocation,
                        "My Location",
                        tint = if (isDarkMode) Color.Black else Color.White
                    )
                }
            }

            // CHANGE: Added button to get ETA and distance
            if (selectedRoute != null && (selectedRoute.workerLat != 0.0 || selectedRoute.workerLng != 0.0)) {
                FloatingActionButton(
                    onClick = {
                        try {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                if (location != null) {
                                    val userLatLng = LatLng(location.latitude, location.longitude)
                                    val truckLatLng = LatLng(selectedRoute.workerLat, selectedRoute.workerLng)

                                    mapViewModel.getEtaAndDistance(userLatLng, truckLatLng, apiKey) // Replace with your API key
                                }
                            }
                        } catch (e: SecurityException) {
                            // Handle exception
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 200.dp),
                    containerColor = primaryColor
                ) {
                    Icon(
                        Icons.Default.Directions,
                        "Get ETA & Distance to Truck",
                        tint = if (isDarkMode) Color.Black else Color.White
                    )
                }
            }

            if (etaAndDistanceState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 280.dp)
                )
            } else if (etaAndDistanceState.error != null) {
                Text(
                    text = "Error: ${etaAndDistanceState.error}",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 280.dp)
                )
            } else if (etaAndDistanceState.eta.isNotEmpty() && etaAndDistanceState.distance.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 260.dp, start = 16.dp, end = 16.dp)
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(8.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor.copy(alpha = 0.96f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "To truck:",
                            style = MaterialTheme.typography.titleMedium,
                            color = primaryColor
                        )
                        HorizontalDivider(color = onSurfaceColor.copy(alpha = 0.2f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "ETA: ${etaAndDistanceState.eta}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = onSurfaceColor
                            )
                            Text(
                                text = "Distance: ${etaAndDistanceState.distance}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = onSurfaceColor
                            )
                        }
                    }
                }
            }

            if (selectedRoute != null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(0.95f),
                    elevation = CardDefaults.cardElevation(8.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor.copy(alpha = 0.96f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = selectedRoute.routeId,
                            style = MaterialTheme.typography.titleMedium,
                            color = primaryColor
                        )
                        HorizontalDivider(color = onSurfaceColor.copy(alpha = 0.2f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total Areas: ${selectedRoute.areaProgress.size}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = onSurfaceColor
                            )
                            val completed = selectedRoute.areaProgress.count { it.isCompleted }
                            Text(
                                text = "Completed: $completed",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isDarkMode) Color(0xFF81C784) else MaterialTheme.colorScheme.secondary
                            )
                        }
                        val progress = selectedRoute.areaProgress.takeIf { it.isNotEmpty() }
                            ?.let { it.count { area -> area.isCompleted }.toFloat() / it.size.toFloat() } ?: 0f
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth(),
                            color = primaryColor,
                            trackColor = onSurfaceColor.copy(alpha = 0.2f)
                        )
                        Text(
                            text = "${(progress * 100).toInt()}% Complete",
                            style = MaterialTheme.typography.bodySmall,
                            color = onSurfaceColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            if (routeState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = primaryColor
                )
            }
        }
    }
}

@Composable
private fun MapControlButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    isDarkMode: Boolean,
    primaryColor: Color,
    onSurfaceColor: Color
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode)
                primaryColor.copy(alpha = 0.8f)
            else onSurfaceColor.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(16.dp),
                tint = if (isDarkMode) Color.Black else onSurfaceColor
            )
            Text(
                text = text,
                color = if (isDarkMode) Color.Black else onSurfaceColor,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}




private fun createTruckMarkerBitmapDescriptor(context: Context, color: Int, size: Int = 128): BitmapDescriptor? {
    val truckDrawable = ContextCompat.getDrawable(context, R.drawable.ic_truck_solid) ?: return null
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val paint = Paint().apply {
        this.color = color
        isAntiAlias = true
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

    truckDrawable.setTint(android.graphics.Color.WHITE)
    val inset = (size * 0.2f).toInt()
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