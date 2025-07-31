package com.example.smartwaste_user.data.models

data class UserModel(
    var userId:String="",
    val email:String="",
    val name:String="",
    val address : String="",
    val phoneNumber : String="",
    val profileImageUrl: String=""
)
