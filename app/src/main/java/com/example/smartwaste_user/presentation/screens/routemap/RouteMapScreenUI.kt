package com.example.smartwaste_user.presentation.screens.routemap

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

enum class MapType {
    NORMAL, SATELLITE, DARK
}

// Dark tile sources
class CartoDarkTileSource : OnlineTileSourceBase(
    "Carto Dark Matter",
    0, 18, 256, ".png",
    arrayOf("https://cartodb-basemaps-a.global.ssl.fastly.net/dark_all/")
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        val zoom = MapTileIndex.getZoom(pMapTileIndex)
        val x = MapTileIndex.getX(pMapTileIndex)
        val y = MapTileIndex.getY(pMapTileIndex)
        return "$mBaseUrl$zoom/$x/$y$mImageFilenameEnding"
    }
}

class StamenTonerTileSource : OnlineTileSourceBase(
    "Stamen Toner",
    0, 18, 256, ".png",
    arrayOf("https://stamen-tiles.a.ssl.fastly.net/toner/")
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        val zoom = MapTileIndex.getZoom(pMapTileIndex)
        val x = MapTileIndex.getX(pMapTileIndex)
        val y = MapTileIndex.getY(pMapTileIndex)
        return "$mBaseUrl$zoom/$x/$y$mImageFilenameEnding"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteMapScreenUI(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentMapType by rememberSaveable { mutableStateOf(MapType.NORMAL) }
    var isDarkMode by rememberSaveable { mutableStateOf(false) }
    
    // Initialize OSMDroid configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Route Map",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Map View
            AndroidView(
                factory = { context ->
                    MapView(context).apply {
                        val mapController: IMapController = controller
                        mapController.setZoom(15.0)
                        mapController.setCenter(GeoPoint(23.0225, 72.5714)) // Ahmedabad coordinates
                        
                        // Set initial tile source
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        
                        // Add sample user location marker (80px size)
                        val userMarker = Marker(this)
                        userMarker.position = GeoPoint(23.0225, 72.5714)
                        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        userMarker.title = "Your Location"
                        overlays.add(userMarker)
                        
                        // Add sample area markers (144px size - 3x bigger)
                        val areaMarkers = listOf(
                            GeoPoint(23.0245, 72.5734) to "Area 1",
                            GeoPoint(23.0265, 72.5754) to "Area 2",
                            GeoPoint(23.0285, 72.5774) to "Area 3"
                        )
                        
                        areaMarkers.forEach { (position, title) ->
                            val areaMarker = Marker(this)
                            areaMarker.position = position
                            areaMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            areaMarker.title = title
                            overlays.add(areaMarker)
                        }
                        
                        // Add sample polyline for route
                        val routePolyline = Polyline()
                        routePolyline.addPoint(GeoPoint(23.0225, 72.5714))
                        routePolyline.addPoint(GeoPoint(23.0245, 72.5734))
                        routePolyline.addPoint(GeoPoint(23.0265, 72.5754))
                        routePolyline.addPoint(GeoPoint(23.0285, 72.5774))
                        routePolyline.color = android.graphics.Color.BLUE
                        routePolyline.width = 5f
                        overlays.add(routePolyline)
                    }
                },
                update = { mapView ->
                    // Update tile source based on current map type and dark mode
                    val tileSource = when {
                        currentMapType == MapType.SATELLITE -> TileSourceFactory.BING_AERIAL
                        currentMapType == MapType.NORMAL && isDarkMode -> CartoDarkTileSource()
                        currentMapType == MapType.NORMAL && !isDarkMode -> TileSourceFactory.MAPNIK
                        else -> TileSourceFactory.MAPNIK
                    }
                    mapView.setTileSource(tileSource)
                    mapView.invalidate()
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Map type controls in top-right corner
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Normal Map Button
                    MapTypeButton(
                        icon = Icons.Default.Map,
                        label = "Normal",
                        isSelected = currentMapType == MapType.NORMAL,
                        onClick = { currentMapType = MapType.NORMAL }
                    )
                    
                    // Satellite Map Button
                    MapTypeButton(
                        icon = Icons.Default.Satellite,
                        label = "Satellite",
                        isSelected = currentMapType == MapType.SATELLITE,
                        onClick = { 
                            currentMapType = MapType.SATELLITE
                            // Dark mode doesn't apply to satellite
                            if (isDarkMode) isDarkMode = false
                        }
                    )
                    
                    // Dark Mode Toggle Button (only available for Normal map)
                    if (currentMapType == MapType.NORMAL) {
                        MapTypeButton(
                            icon = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            label = if (isDarkMode) "Light" else "Dark",
                            isSelected = isDarkMode,
                            onClick = { isDarkMode = !isDarkMode }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MapTypeButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = contentColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}