package com.example.smartwaste_user.domain.usecase.holidayusecase

import com.example.smartwaste_user.common.ResultState
import com.example.smartwaste_user.domain.repo.holidayrepo.HolidayRepositry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllHolidaysUseCase @Inject constructor(
    private val holidayRepositry: HolidayRepositry
) {

    suspend fun getAllHolidays() =holidayRepositry.getAllHolidays()
}