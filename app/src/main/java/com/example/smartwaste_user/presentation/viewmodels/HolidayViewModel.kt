package com.example.smartwaste_user.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartwaste_user.common.ResultState
import com.example.smartwaste_user.data.models.HolidayModel
import com.example.smartwaste_user.domain.usecase.holidayusecase.GetAllHolidaysUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HolidayViewModel @Inject constructor(
    private val getAllHolidaysUseCase: GetAllHolidaysUseCase

) : ViewModel() {

    private val _holidayState = MutableStateFlow(CommonHolidayState<List<HolidayModel>>())
    val holidayState = _holidayState.asStateFlow()

    fun getAllHolidays() {
        viewModelScope.launch(Dispatchers.IO) {
            getAllHolidaysUseCase.getAllHolidays().collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        _holidayState.value = CommonHolidayState(isLoading = true)
                    }

                    is ResultState.Success -> {
                        _holidayState.value = CommonHolidayState(success = result.data)
                    }

                    is ResultState.Error -> {
                        _holidayState.value = CommonHolidayState(error = result.error)
                    }
                }

            }

        }
    }


}

data class CommonHolidayState<T>(
    val isLoading: Boolean = false,
    val success: T? = null,
    val error: String = ""

)