package com.example.smartwaste_user.presentation.navigation

import android.os.Parcelable
import com.example.smartwaste_user.data.models.AreaInfo
import com.example.smartwaste_user.data.models.RouteModel
import kotlinx.serialization.Serializable

sealed class Routes{
    @Serializable
    object SignUpScreen


    @Serializable
    object LoginScreen


    @Serializable
    object OnBoardingScreen

    @Serializable
    object HomeScreen

    @Serializable
    object NotificationScreen

    @Serializable
    object ReportScreen

    @Serializable
    object ProfileScreen

    @Serializable
    object VerifyEmailScreen

    @Serializable
    object MakeReportScreen

    @Serializable
    object MakeComplaintScreen

    @Serializable
    object RequestExtraServiceScreen


    @Serializable
    data class RouteMapScreen(
        val routeId: String
    )

}


sealed class SubNavigation {

    @Serializable
    object AuthRoutes

    @Serializable
    object HomeRoutes

    @Serializable
    object OnBoardingScreen

    @Serializable
    object VerifyEmailRoutes

}

