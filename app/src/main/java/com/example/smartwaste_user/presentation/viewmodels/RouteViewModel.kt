package com.example.smartwaste_user.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartwaste_user.common.ResultState
import com.example.smartwaste_user.data.models.RouteModel
import com.example.smartwaste_user.domain.repo.routerepo.RouteRepositry
import com.example.smartwaste_user.domain.usecase.routeusecase.GetAllRoutesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RouteViewModel @Inject constructor(
    private val getAllRoutesUseCase: GetAllRoutesUseCase
) : ViewModel(){

    private val _routeState = MutableStateFlow(CommonRouteState<List<RouteModel>>())
    val routeState = _routeState.asStateFlow()


    fun getAllRoutes(){
        viewModelScope.launch(Dispatchers.IO) {

            getAllRoutesUseCase.getAllRoutesUseCase().collect {
                when(it){
                    is ResultState.Loading -> {
                        _routeState.value = CommonRouteState(isLoading = true)
                    }
                    is ResultState.Success -> {
                        _routeState.value = CommonRouteState(success = it.data)
                    }
                    is ResultState.Error -> {
                        _routeState.value = CommonRouteState(error = it.error.toString())
                    }
                }
            }

        }

    }




}

data class CommonRouteState<T>(
    val isLoading: Boolean = false,

    val success : T? = null,
    val error : String=""
)