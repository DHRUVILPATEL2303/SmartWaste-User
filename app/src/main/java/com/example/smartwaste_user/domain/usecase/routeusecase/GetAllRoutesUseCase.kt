package com.example.smartwaste_user.domain.usecase.routeusecase

import com.example.smartwaste_user.domain.repo.routerepo.RouteRepositry
import javax.inject.Inject

class GetAllRoutesUseCase @Inject constructor(
    private val routeRepositry: RouteRepositry
) {
    suspend fun getAllRoutesUseCase() = routeRepositry.getAllRoutes()
}