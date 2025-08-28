package com.example.smartwaste_user.data.repoimpl.DirectionsRepositryimpl

import com.example.smartwaste_user.data.remote.NetworkModule
import com.example.smartwaste_user.domain.repo.directionrepo.DirectionsRepositry
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

class DirectionRepositryImpl @Inject constructor(

) : DirectionsRepositry{

    private val api= NetworkModule.provideORSApi()
    override suspend fun fetchRoute(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    ): List<GeoPoint> = withContext(Dispatchers.IO){
        try {
            val start = "${startLng},${startLat}" // ORS expects "lng,lat"
            val end = "${endLng},${endLat}"
            val response = api.getDrivingRoute("eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6ImQwNmU1ZDNjMmVhZTQxYjI5ODdjZThjMGVhNTBhNTc2IiwiaCI6Im11cm11cjY0In0=", start, end)

            val coords = response.features
                ?.firstOrNull()
                ?.geometry
                ?.coordinates
                ?: emptyList()

            // Convert [lng,lat] -> GeoPoint(lat, lng)
            coords.mapNotNull { pair ->
                if (pair.size >= 2) GeoPoint(pair[1], pair[0]) else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getEta(
        origin: LatLng,
        destination: LatLng,
        apiKey: String
    ): String = withContext(Dispatchers.IO) {
        try {
            val api = NetworkModule.provideDirectionsApi()

            val response = api.getDirections(
                origin = "${origin.latitude},${origin.longitude}",
                destination = "${destination.latitude},${destination.longitude}",
                mode = "driving",
                apiKey = apiKey
            )

            val durationText = response.routes
                ?.firstOrNull()
                ?.legs
                ?.firstOrNull()
                ?.duration
                ?.text

            durationText ?: "N/A"
        } catch (e: Exception) {
            e.printStackTrace()
            "N/A"
        }
    }


}