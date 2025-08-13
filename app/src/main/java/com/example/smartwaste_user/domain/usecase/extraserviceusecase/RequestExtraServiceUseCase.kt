package com.example.smartwaste_user.domain.usecase.extraserviceusecase

import com.example.smartwaste_user.data.models.ExtraServiceModel
import com.example.smartwaste_user.domain.repo.extraservice.ExtraServiceRepositry
import javax.inject.Inject

class RequestExtraServiceUseCase @Inject constructor(
    private val extraServiceRepositry: ExtraServiceRepositry
)
{

    suspend fun requestExtraService(extraServiceModel: ExtraServiceModel) =extraServiceRepositry.requestExtraService(extraServiceModel)
}