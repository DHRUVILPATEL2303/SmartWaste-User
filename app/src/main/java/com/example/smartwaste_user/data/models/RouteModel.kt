package com.example.smartwaste_user.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class AreaInfo(
    val areaId: String = "",
    val areaName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) : Parcelable

@Serializable
@Parcelize
data class RouteModel(
    val id: String = "",
    val name: String = "",
    val areaList: List<AreaInfo> = emptyList(),
    val isActive: Boolean = true
) : Parcelable