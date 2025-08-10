package com.example.smartwaste_user.data.models

data class UserModel(
    var userId:String="",
    val email:String="",
    val name:String="",
    val address : String="",
    val phoneNumber : String="",
    val profileImageUrl: String="",
    val areaName:String="",
    val areaId:String="",
    val routeId:String="",
    val routeName:String="",
)
