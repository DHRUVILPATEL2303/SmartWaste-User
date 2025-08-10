package com.example.smartwaste_user.domain.repo.routerepo

import com.example.smartwaste_user.common.ResultState
import com.example.smartwaste_user.data.models.RouteModel
import kotlinx.coroutines.flow.Flow

interface RouteRepositry {
    suspend fun getAllRoutes() : Flow<ResultState<List<RouteModel>>>
}