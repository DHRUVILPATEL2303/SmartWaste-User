package com.example.smartwaste_user.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartwaste_user.common.ResultState
import com.example.smartwaste_user.data.models.WorkerFeedBackModel
import com.example.smartwaste_user.domain.usecase.workerfeebackusecase.GiveFeedBackToWorkerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class WorkerFeedBackViewModel @Inject constructor(
    private val giveFeedBackToWorkerUseCase: GiveFeedBackToWorkerUseCase
)  : ViewModel(){




    private val _workerFeedBackState = MutableStateFlow(CommonWorkerFeedBackState<String>())
    val workerFeedBackState = _workerFeedBackState.asStateFlow()


    fun giveFeedBack(feedBackModel: WorkerFeedBackModel){
        viewModelScope.launch(Dispatchers.IO) {
            giveFeedBackToWorkerUseCase.giveFeedBacktoWorker(feedBackModel).collect{

                    when(it){
                        is ResultState.Loading -> {
                            _workerFeedBackState.value = CommonWorkerFeedBackState(isLoading = true)
                        }

                        is ResultState.Success -> {
                            _workerFeedBackState.value = CommonWorkerFeedBackState(success = it.data)
                        }

                        is ResultState.Error -> {
                            _workerFeedBackState.value = CommonWorkerFeedBackState(error = it.error)
                        }



                    }

            }

        }
    }

}


data class CommonWorkerFeedBackState<T>(
    val isLoading: Boolean = false,
    val success: T? = null,
    val error: String = ""
)