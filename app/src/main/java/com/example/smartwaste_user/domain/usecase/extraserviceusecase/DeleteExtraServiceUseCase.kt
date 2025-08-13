package com.example.smartwaste_user.domain.usecase.extraserviceusecase

import com.example.smartwaste_user.domain.repo.extraservice.ExtraServiceRepositry
import javax.inject.Inject

class DeleteExtraServiceUseCase @Inject constructor(
    private val extraServiceRepositry: ExtraServiceRepositry
) {

    suspend fun deleteExtraService(id:String) = extraServiceRepositry.deleteExtraService(id)
}