package com.example.smartwaste_user.domain.usecase.directionusecase

import com.example.smartwaste_user.domain.repo.directionrepo.DirectionsRepositry
import javax.inject.Inject

class GetRouteDirectionUseCase @Inject constructor(
    private val directionsRepositry: DirectionsRepositry
) {

    suspend fun getRouteDirection(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    )=directionsRepositry.fetchRoute(
        startLat = startLat,
        startLng = startLng,
        endLat = endLat,
        endLng=endLng

    )
}