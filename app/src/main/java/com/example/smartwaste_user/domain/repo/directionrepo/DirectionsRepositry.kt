package com.example.smartwaste_user.domain.repo.directionrepo

import org.osmdroid.util.GeoPoint

interface DirectionsRepositry {

    suspend fun fetchRoute(
        startLat: Double, startLng: Double,
        endLat: Double, endLng: Double
    ) : List<GeoPoint>
}