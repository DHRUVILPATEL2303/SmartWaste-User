package com.example.smartwaste_user.presentation.screens.routemap

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartwaste_user.presentation.viewmodels.RouteProgressViewModel
import com.example.smartwaste_user.presentation.viewmodels.directionviewmodel.RouteMapViewModel
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.config.Configuration
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import android.Manifest
import androidx.compose.runtime.LaunchedEffect
import androidx.core.graphics.drawable.toBitmap
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.isGranted

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun RouteMapScreenUI(
    navController: NavHostController,
    routeId: String,
    routeViewModel: RouteProgressViewModel = hiltViewModel(),
    mapViewModel: RouteMapViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    var isDarkMode by rememberSaveable { mutableStateOf(false) }

    val backgroundColor = if (isDarkMode) Color(0xFF121212) else MaterialTheme.colorScheme.background
    val surfaceColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val onSurfaceColor = if (isDarkMode) Color.White else Color.Black
    val primaryColor = if (isDarkMode) Color(0xFF90CAF9) else MaterialTheme.colorScheme.primary
    val cardColor = if (isDarkMode) Color(0xFF2D2D2D) else Color.White

    val tileSource = if (isDarkMode) {
        createDarkMapTileSource()
    } else {
        TileSourceFactory.MAPNIK
    }

    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    )

    val routeState = routeViewModel.routeProgressState.collectAsState().value
    val mapState = mapViewModel.state.collectAsState().value

    LaunchedEffect(Unit) {
        // Initialize OSMdroid configuration
        Configuration.getInstance().userAgentValue = "SmartWasteUser"

        routeViewModel.getallRouteProgress()
        locationPermissionsState.launchMultiplePermissionRequest()
    }

    val selectedRoute = routeState.succcess?.find { it.routeId == routeId }

    LaunchedEffect(selectedRoute) {
        selectedRoute?.let {
            mapViewModel.loadRoute(it.areaProgress)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = selectedRoute?.routeId ?: "Route Map",
                        color = onSurfaceColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = onSurfaceColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = onSurfaceColor,
                    navigationIconContentColor = onSurfaceColor
                )
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
            var mapView: MapView? by remember { mutableStateOf(null) }
            var myLocationOverlay: MyLocationNewOverlay? by remember { mutableStateOf(null) }

            AndroidView(
                factory = {
                    MapView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setTileSource(tileSource)
                        controller.setZoom(13.0)
                        setMultiTouchControls(true)

                        if (locationPermissionsState.permissions.any { it.status.isGranted }) {
                            val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this).apply {
                                enableMyLocation()
                                enableFollowLocation()
                                setPersonIcon(createUserLocationIcon(context, 80).toBitmap())
                                setDirectionIcon(createUserLocationIcon(context, 80).toBitmap())
                                setPersonHotspot(40f, 40f)
                            }
                            overlays.add(locationOverlay)
                            myLocationOverlay = locationOverlay
                        }

                        selectedRoute?.areaProgress?.firstOrNull()?.let {
                            controller.setCenter(GeoPoint(it.latitude, it.longitude))
                        }
                        mapView = this
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 180.dp),
                update = { mv ->
                    mv.setTileSource(tileSource)

                    val locationOverlay = mv.overlays.find { it is MyLocationNewOverlay } as? MyLocationNewOverlay
                    mv.overlays.clear()

                    locationOverlay?.let { overlay ->
                        if (locationPermissionsState.permissions.any { it.status.isGranted }) {
                            mv.overlays.add(overlay)
                        }
                    }

                    mapState.markers.forEach { area ->
                        val marker = Marker(mv).apply {
                            position = GeoPoint(area.latitude, area.longitude)
                            title = area.areaName
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                            if (area.isCompleted) {
                                setIcon(createColoredMarker(context, android.graphics.Color.GREEN, 144))
                            } else {
                                setIcon(createColoredMarker(context, android.graphics.Color.RED, 144))
                            }

                            setOnMarkerClickListener { marker, mapView ->
                                marker.showInfoWindow()
                                true
                            }
                        }
                        mv.overlays.add(marker)
                    }

                    val allBoundsPoints = mutableListOf<GeoPoint>()
                    mapState.polylines.forEach { leg ->
                        if (leg.isNotEmpty()) {
                            val pl = Polyline().apply {
                                setPoints(leg)
                                outlinePaint.strokeWidth = 8f
                                outlinePaint.color = android.graphics.Color.parseColor("#388E3C")
                                outlinePaint.alpha = 180
                            }
                            mv.overlays.add(pl)
                            allBoundsPoints.addAll(leg)
                        }
                    }

                    val pointsForBounds = if (allBoundsPoints.isNotEmpty()) {
                        allBoundsPoints
                    } else {
                        mapState.markers.map { GeoPoint(it.latitude, it.longitude) }
                    }

                    if (pointsForBounds.isNotEmpty()) {
                        val box = BoundingBox.fromGeoPoints(pointsForBounds)
                        mv.zoomToBoundingBox(box, true, 120)
                    }

                    mv.invalidate()
                }
            )

            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = surfaceColor.copy(alpha = 0.95f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Map",
                        modifier = Modifier.size(18.dp),
                        tint = primaryColor
                    )

                    Text(
                        text = "Map",
                        style = MaterialTheme.typography.bodyMedium,
                        color = onSurfaceColor
                    )

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(onSurfaceColor.copy(alpha = 0.3f))
                    )

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkMode)
                                primaryColor.copy(alpha = 0.8f)
                            else onSurfaceColor.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        TextButton(
                            onClick = { isDarkMode = !isDarkMode },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme",
                                modifier = Modifier.size(16.dp),
                                tint = if (isDarkMode) Color.Black else onSurfaceColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isDarkMode) "Light" else "Dark",
                                color = if (isDarkMode) Color.Black else onSurfaceColor,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }


            if (locationPermissionsState.permissions.any { it.status.isGranted }) {
                FloatingActionButton(
                    onClick = {
                        myLocationOverlay?.let { overlay ->
                            overlay.myLocation?.let { location ->
                                mapView?.controller?.animateTo(location)
                                mapView?.controller?.setZoom(16.0)
                            } ?: run {
                                overlay.enableMyLocation()
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 200.dp),
                    containerColor = primaryColor
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "My Location",
                        tint = if (isDarkMode) Color.Black else Color.White
                    )
                }
            }

            DisposableEffect(Unit) {
                onDispose {
                    myLocationOverlay?.disableMyLocation()
                    myLocationOverlay?.disableFollowLocation()
                    mapView?.onPause()
                    mapView?.onDetach()
                    mapView = null
                    myLocationOverlay = null
                }
            }


            if (selectedRoute != null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(0.95f),
                    elevation = CardDefaults.cardElevation(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = cardColor.copy(alpha = 0.96f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = selectedRoute.routeId ?: "Route Details",
                            style = MaterialTheme.typography.titleMedium,
                            color = primaryColor
                        )

                        HorizontalDivider(
                            color = onSurfaceColor.copy(alpha = 0.2f)
                        )

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

                        val completed = selectedRoute.areaProgress.count { it.isCompleted }
                        val progress = if (selectedRoute.areaProgress.isNotEmpty()) {
                            completed.toFloat() / selectedRoute.areaProgress.size.toFloat()
                        } else 0f

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

            if (selectedRoute == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = primaryColor
                )
            }
        }
    }
}


private fun createColoredMarker(context: Context, color: Int, size: Int = 48): Drawable {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val paint = Paint().apply {
        this.color = color
        isAntiAlias = true
    }

    val centerX = size / 2f
    val centerY = size / 2f
    val radius = size / 3f

    canvas.drawCircle(centerX, centerY - radius / 2, radius, paint)

    val path = android.graphics.Path()
    path.moveTo(centerX - radius / 2, centerY)
    path.lineTo(centerX + radius / 2, centerY)
    path.lineTo(centerX, centerY + radius)
    path.close()
    canvas.drawPath(path, paint)

    val borderPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = (size / 12f)
        isAntiAlias = true
    }

    canvas.drawCircle(centerX, centerY - radius / 2, radius, borderPaint)
    canvas.drawPath(path, borderPaint)

    return BitmapDrawable(context.resources, bitmap)
}


private fun createUserLocationIcon(context: Context, size: Int = 80): Drawable {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val centerX = size / 2f
    val centerY = size / 2f
    val radius = size / 3f

    val outerPaint = Paint().apply {
        color = android.graphics.Color.BLUE
        isAntiAlias = true
        alpha = 80
    }
    canvas.drawCircle(centerX, centerY, radius, outerPaint)

    val middlePaint = Paint().apply {
        color = android.graphics.Color.BLUE
        isAntiAlias = true
        alpha = 150
    }
    canvas.drawCircle(centerX, centerY, radius * 0.7f, middlePaint)

    val innerPaint = Paint().apply {
        color = android.graphics.Color.BLUE
        isAntiAlias = true
    }
    canvas.drawCircle(centerX, centerY, radius / 2.5f, innerPaint)

    val borderPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = size / 16f
        isAntiAlias = true
    }
    canvas.drawCircle(centerX, centerY, radius / 2.5f, borderPaint)

    return BitmapDrawable(context.resources, bitmap)
}


private fun createDarkMapTileSource(): ITileSource {
    return object : OnlineTileSourceBase(
        "CartoDarkMatter",
        0, 18, 256, ".png",
        arrayOf("https://cartodb-basemaps-a.global.ssl.fastly.net/dark_all/")
    ) {
        override fun getTileURLString(pMapTileIndex: Long): String {
            val zoom = MapTileIndex.getZoom(pMapTileIndex)
            val x = MapTileIndex.getX(pMapTileIndex)
            val y = MapTileIndex.getY(pMapTileIndex)
            return "${baseUrl[0]}$zoom/$x/$y.png"
        }
    }
}