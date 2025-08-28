package com.example.smartwaste_user.domain.usecase.directionusecase

import com.example.smartwaste_user.domain.repo.directionrepo.DirectionsRepositry
import com.google.android.gms.maps.model.LatLng
import javax.inject.Inject

class GetETAUseCase @Inject constructor(
    private val directionsRepositry: DirectionsRepositry
) {

    suspend fun getEtaUseCase(
        origin: LatLng,
        destination: LatLng,
        apiKey: String
    ): String =
        directionsRepositry.getEta(
            origin = origin,
            destination = destination,
            apiKey = apiKey
    )
}