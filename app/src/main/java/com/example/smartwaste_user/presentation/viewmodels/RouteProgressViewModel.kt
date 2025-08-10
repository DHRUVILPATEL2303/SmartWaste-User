package com.example.smartwaste_user.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartwaste_user.common.ResultState
import com.example.smartwaste_user.data.models.RouteProgressModel
import com.example.smartwaste_user.domain.usecase.routeprogresusecase.GetAllRoutesProgresssUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class RouteProgressViewModel @Inject constructor(
    private val getAllRoutesProgresssUseCase: GetAllRoutesProgresssUseCase
) : ViewModel(){

    private val _routeProgressState = MutableStateFlow(CommonRoutesProgressState<List<RouteProgressModel>>())
    val routeProgressState = _routeProgressState.asStateFlow()


    fun getallRouteProgress(){
        viewModelScope.launch(Dispatchers.IO) {
            getAllRoutesProgresssUseCase.getAllRoutesProgressUseCase().collect {
                when(it){
                    is ResultState.Loading -> {
                        _routeProgressState.value = CommonRoutesProgressState(isLoading = true)
                    }
                    is ResultState.Success -> {
                        _routeProgressState.value = CommonRoutesProgressState(succcess = it.data, isLoading = false)
                    }

                    is ResultState.Error -> {
                        _routeProgressState.value = CommonRoutesProgressState(error = it.error, isLoading = false)
                    }

                }
            }

        }
    }

}

data class CommonRoutesProgressState<T>(
    val isLoading : Boolean=false,
    val succcess : T?=null,
    val error:String=""
)