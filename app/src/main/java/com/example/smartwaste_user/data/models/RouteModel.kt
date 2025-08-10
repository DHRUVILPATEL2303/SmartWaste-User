package com.example.smartwaste_user.data.models

data class AreaInfo(
    val areaId: String = "",
    val areaName: String = ""
)
data class RouteModel(
    val id: String = "",
    val name: String = "",
    val areaList: List<AreaInfo> = emptyList(),
    val isActive: Boolean = true
)