package com.example.smartwaste_user.presentation.viewmodels.directionviewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartwaste_user.data.models.AreaInfo
import com.example.smartwaste_user.data.models.AreaProgress
import com.example.smartwaste_user.domain.usecase.directionusecase.GetETAUseCase
import com.example.smartwaste_user.domain.usecase.directionusecase.GetRouteDirectionUseCase
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import javax.inject.Inject


data class RouteUiState(
    val isLoading: Boolean = false,
    val markers: List<AreaProgress> = emptyList(),
    val polylines: List<List<GeoPoint>> = emptyList(),
    val error: String? = null
)

data class EtaState(
    val isLoading: Boolean = false,
    val etaText: String = "",
    val error: String? = null
)

data class EtaAndDistanceState(
    val isLoading: Boolean = false,
    val eta: String = "",
    val distance: String = "",
    val error: String? = null
)


@HiltViewModel
class RouteMapViewModel @Inject constructor(
    private val getRouteDirectionUseCase: GetRouteDirectionUseCase,
    private val getETAUseCase: GetETAUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RouteUiState())
    val state = _state.asStateFlow()

    private val _etaState = MutableStateFlow(EtaState())
    val etaState = _etaState.asStateFlow()

    private val _etaAndDistanceState = MutableStateFlow(EtaAndDistanceState())
    val etaAndDistanceState = _etaAndDistanceState.asStateFlow()

    fun loadRoute(areaList: List<AreaProgress>) {
        if (areaList.isEmpty()) {
            _state.value = RouteUiState(isLoading = false, markers = emptyList(), polylines = emptyList())
            return
        }

        _state.value = _state.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val legs = mutableListOf<List<GeoPoint>>()

            for (i in 0 until areaList.size - 1) {
                val a = areaList[i]
                val b = areaList[i + 1]
                val route = getRouteDirectionUseCase.getRouteDirection(
                    startLat = a.latitude, startLng = a.longitude,
                    endLat = b.latitude, endLng = b.longitude
                )
                if (route.isNotEmpty()) {
                    legs.add(route)
                }
            }

            _state.value = RouteUiState(
                isLoading = false,
                markers = areaList,
                polylines = legs,
                error = null
            )
        }
    }


    fun getEta(origin: LatLng, destination: LatLng, apiKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _etaState.value = EtaState(isLoading = true)

            try {
                val etaText = getETAUseCase.getEtaUseCase(origin, destination, apiKey)
                _etaState.value = EtaState(
                    isLoading = false,
                    etaText = etaText
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _etaState.value = EtaState(
                    isLoading = false,
                    etaText = "",
                    error = e.message ?: "Failed to fetch ETA"
                )
            }
        }
    }

    fun getEtaAndDistance(origin: LatLng, destination: LatLng, apiKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _etaAndDistanceState.value = EtaAndDistanceState(isLoading = true)

            try {
                val etaText = getETAUseCase.getEtaUseCase(origin, destination, apiKey)
                val distance = calculateDistance(origin, destination)
                _etaAndDistanceState.value = EtaAndDistanceState(
                    isLoading = false,
                    eta = etaText,
                    distance = String.format("%.2f km", distance)
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _etaAndDistanceState.value = EtaAndDistanceState(
                    isLoading = false,
                    error = e.message ?: "Failed to fetch ETA and distance"
                )
            }
        }
    }

    private fun calculateDistance(startLatLng: LatLng, endLatLng: LatLng): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            startLatLng.latitude, startLatLng.longitude,
            endLatLng.latitude, endLatLng.longitude,
            results
        )
        return results[0] / 1000 // convert to km
    }
}