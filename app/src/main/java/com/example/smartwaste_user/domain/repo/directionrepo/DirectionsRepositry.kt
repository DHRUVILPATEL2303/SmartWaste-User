package com.example.smartwaste_user.domain.repo.directionrepo

import com.google.android.gms.maps.model.LatLng
import org.osmdroid.util.GeoPoint

interface DirectionsRepositry {

    suspend fun fetchRoute(
        startLat: Double, startLng: Double,
        endLat: Double, endLng: Double
    ) : List<GeoPoint>


    suspend fun getEta(
        origin: LatLng,
        destination: LatLng,
        apiKey: String
    ) : String
}