package com.example.smartwaste_user.presentation.viewmodels.editorialviewmodel

import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartwaste_user.common.ResultState
import com.example.smartwaste_user.data.models.EdiorialModel
import com.example.smartwaste_user.domain.usecase.ediorialusecase.GetAllEditorialUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.compose
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class EditorialViewModel @Inject constructor(
    private val getAllEditorialUseCase: GetAllEditorialUseCase
) : ViewModel(){

    private val _getAllEditorialState= MutableStateFlow(CommonEditorialState<List<EdiorialModel>>())
    val getAllEditorialState=_getAllEditorialState.asStateFlow()

    fun getAllEditorials(){
        viewModelScope.launch(Dispatchers.IO) {
            getAllEditorialUseCase.getAllEditorials().collect {

                when(it){
                    is ResultState.Error -> {
                        _getAllEditorialState.value=CommonEditorialState(error = it.error, isLoading = false)

                    }
                    ResultState.Loading -> {
                        _getAllEditorialState.value=CommonEditorialState(isLoading = true)

                    }
                    is ResultState.Success -> {
                        _getAllEditorialState.value=CommonEditorialState(success = it.data, isLoading = false)
                    }
                }
            }
        }
    }
}

data class CommonEditorialState<T>(
    val isLoading : Boolean=false,
    val success:  T?=null,
    val error : String=""
)