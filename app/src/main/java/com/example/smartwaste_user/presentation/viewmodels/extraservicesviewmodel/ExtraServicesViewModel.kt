package com.example.smartwaste_user.presentation.viewmodels.extraservicesviewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartwaste_user.common.ResultState
import com.example.smartwaste_user.data.models.ExtraServiceModel
import com.example.smartwaste_user.domain.usecase.extraserviceusecase.DeleteExtraServiceUseCase
import com.example.smartwaste_user.domain.usecase.extraserviceusecase.GetAllExtraServiceUseCase
import com.example.smartwaste_user.domain.usecase.extraserviceusecase.RequestExtraServiceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ExtraServicesViewModel @Inject constructor(
    private val getAllExtraServiceUseCase: GetAllExtraServiceUseCase,
    private val  requestExtraServiceUseCase: RequestExtraServiceUseCase,
    private val deleteExtraServiceUseCase: DeleteExtraServiceUseCase
) : ViewModel(){


    private val _requestExtraServiceState = MutableStateFlow(CommonExtraServiceState<String>())
    val requestExtraServiceState = _requestExtraServiceState.asStateFlow()

    private val _deleteExtraServiceState = MutableStateFlow(CommonExtraServiceState<String>())
    val deleteExtraServiceState = _deleteExtraServiceState.asStateFlow()

    private val _getAllExtraServiceState = MutableStateFlow(CommonExtraServiceState<List<ExtraServiceModel>>())
    val getAllExtraServiceState = _getAllExtraServiceState.asStateFlow()


    fun requestExtraService(extraServiceModel: ExtraServiceModel){
        viewModelScope.launch(Dispatchers.IO) {
            requestExtraServiceUseCase.requestExtraService(extraServiceModel).collect{

                when(it){
                    is ResultState.Loading ->{
                        _requestExtraServiceState.value = CommonExtraServiceState(isLoading = true)
                    }

                    is ResultState.Success ->{
                        _requestExtraServiceState.value = CommonExtraServiceState(succcess = it.data, isLoading = false)
                    }
                    is ResultState.Error ->{
                        _requestExtraServiceState.value = CommonExtraServiceState(error = it.error, isLoading = false)
                    }


                }
            }

        }
    }

    fun deleteExtraService(id:String){
        viewModelScope.launch(Dispatchers.IO) {
            deleteExtraServiceUseCase.deleteExtraService(id).collect{
                when(it){
                    is ResultState.Loading ->{
                        _deleteExtraServiceState.value = CommonExtraServiceState(isLoading = true)
                    }
                    is ResultState.Success ->{
                        _deleteExtraServiceState.value = CommonExtraServiceState(succcess = it.data, isLoading = false)
                    }

                    is ResultState.Error ->{
                        _deleteExtraServiceState.value = CommonExtraServiceState(error = it.error, isLoading = false)
                    }


                }
            }
        }
    }

    fun getAllExtraServices(){
        viewModelScope.launch(Dispatchers.IO) {
            getAllExtraServiceUseCase.getAllEtraServices().collect{


                when(it){
                    is ResultState.Loading ->{
                        _getAllExtraServiceState.value = CommonExtraServiceState(isLoading = true)
                    }

                    is ResultState.Success ->{
                        _getAllExtraServiceState.value = CommonExtraServiceState(succcess = it.data, isLoading = false)
                    }
                    is ResultState.Error ->{
                        _getAllExtraServiceState.value = CommonExtraServiceState(error = it.error, isLoading = false)
                    }

                }
            }

        }
    }




}

data class CommonExtraServiceState<T>(
    val isLoading  : Boolean =false,
    val succcess:  T?=null,
    val error : String =""
)