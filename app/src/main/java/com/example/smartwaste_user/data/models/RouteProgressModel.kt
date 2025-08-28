package com.example.smartwaste_user.data.models

data class RouteProgressModel(
    val routeId: String = "",
    val date: String = "",
    val assignedCollectorId: String = "",
    val assignedDriverId: String = "",
    val assignedTruckId: String = "",
    val areaProgress: List<AreaProgress> = emptyList(),
    val isRouteCompleted: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis(),
    val workerLat : Double=0.0,
    val workerLng : Double =0.0
)

data class AreaProgress(
    val areaId: String = "",
    val areaName: String = "",
    var isCompleted: Boolean = false,
    var completedAt: Long? = null,
    val latitude : Double = 0.0,
    val longitude : Double = 0.0
)