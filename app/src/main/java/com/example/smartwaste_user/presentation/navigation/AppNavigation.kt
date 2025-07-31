package com.example.smartwaste_user.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController

import com.example.smartwaste_user.presentation.screens.OnBoarding.OnBoardingScreenUI
import com.example.smartwaste_user.presentation.screens.auth.SignUpScreenUI
import com.example.smartwaste_user.presentation.viewmodels.AuthViewModel

@Composable
fun AppNavigation(viewModel: AuthViewModel = hiltViewModel<AuthViewModel>()) {


    val navController = rememberNavController()


    NavHost(
        navController=navController,
        startDestination = SubNavigation.OnBoardingScreen
    ){
        navigation<SubNavigation.AuthRoutes>(
            startDestination = Routes.SignUpScreen
        ){

            composable<Routes.SignUpScreen> {
                SignUpScreenUI(navController=navController)

            }

        }

        navigation<SubNavigation.OnBoardingScreen>(
            startDestination = Routes.OnBoardingScreen
        ){
            composable<Routes.OnBoardingScreen> {
                OnBoardingScreenUI(navController=navController)
            }

        }

        navigation<SubNavigation.HomeRoutes>(
            startDestination = Routes.HomeScreen
        ){
            composable<Routes.HomeScreen> {
                HomeScreenUI(navController=navController)
            }
        }
    }

}

@Composable
fun HomeScreenUI(navController: NavHostController) {
}

