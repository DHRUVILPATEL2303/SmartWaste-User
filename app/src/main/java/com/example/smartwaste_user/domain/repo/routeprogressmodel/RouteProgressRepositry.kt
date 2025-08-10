package com.example.smartwaste_user.domain.repo.routeprogressmodel

import com.example.smartwaste_user.common.ResultState
import com.example.smartwaste_user.data.models.RouteProgressModel
import kotlinx.coroutines.flow.Flow

interface RouteProgressRepositry {

    suspend fun getAllRoutesProgress(): Flow<ResultState<List<RouteProgressModel>>>
}