package com.example.smartwaste_user.presentation.navigation

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
}


sealed class SubNavigation {

    @Serializable
    object AuthRoutes

    @Serializable
    object HomeRoutes

    @Serializable
    object OnBoardingScreen

}