package com.example.smartwaste_user.domain.repo.extraservice

import com.example.smartwaste_user.common.ResultState
import com.example.smartwaste_user.data.models.ExtraServiceModel
import kotlinx.coroutines.flow.Flow

interface ExtraServiceRepositry {

    suspend fun requestExtraService(extraServiceModel: ExtraServiceModel) : Flow<ResultState<String>>

    suspend fun deleteExtraService(id:String) : Flow<ResultState<String>>

    suspend fun getAllExtraServices() : Flow<ResultState<List<ExtraServiceModel>>>
}