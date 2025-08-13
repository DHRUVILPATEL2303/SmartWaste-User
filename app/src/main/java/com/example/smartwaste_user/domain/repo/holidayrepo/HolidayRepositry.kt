package com.example.smartwaste_user.domain.repo.holidayrepo

import com.example.smartwaste_user.common.ResultState
import com.example.smartwaste_user.data.models.HolidayModel
import kotlinx.coroutines.flow.Flow

interface HolidayRepositry  {

    suspend fun getAllHolidays() : Flow<ResultState<List<HolidayModel>>>
}