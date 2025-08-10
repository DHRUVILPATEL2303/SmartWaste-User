package com.example.smartwaste_user.data.models

data class ReportModel(
    val reportId:String="",
    val userId:String="",
    val againstDriverId : String="",
    val againstCollectorId : String="",
    val routeId:String="",
    val areaId: String="",
    val status:String="",
    val areaName: String="",
    val reportDate:String=""
)
