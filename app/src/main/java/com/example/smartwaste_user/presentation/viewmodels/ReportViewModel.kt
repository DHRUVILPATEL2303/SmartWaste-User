package com.example.smartwaste_user.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartwaste_user.common.ResultState
import com.example.smartwaste_user.data.models.ReportModel
import com.example.smartwaste_user.domain.usecase.reportusecases.DeleteReportUseCase
import com.example.smartwaste_user.domain.usecase.reportusecases.GetAllReportsUseCase
import com.example.smartwaste_user.domain.usecase.reportusecases.MakeReportUseCase
import com.example.smartwaste_user.domain.usecase.reportusecases.UpdateReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ReportViewModel @Inject constructor(
    private val getAllReportsUseCase: GetAllReportsUseCase,
    private val makeReportUseCase: MakeReportUseCase,
    private val deleteReportUseCase: DeleteReportUseCase,
    private val updateReportUseCase: UpdateReportUseCase
) : ViewModel() {

    private val _reportState = MutableStateFlow(CommonReportState<List<ReportModel>>())
    val reportState: StateFlow<CommonReportState<List<ReportModel>>> = _reportState

    private val _makeReportState = MutableStateFlow(CommonReportState<String>())
    val makeReportState: StateFlow<CommonReportState<String>> = _makeReportState.asStateFlow()

    private val _deleteReportState = MutableStateFlow(CommonReportState<String>())
    val deleteReportState: StateFlow<CommonReportState<String>> = _deleteReportState.asStateFlow()

    private val _updateReportState = MutableStateFlow(CommonReportState<String>())
    val updateReportState: StateFlow<CommonReportState<String>> = _updateReportState.asStateFlow()


    fun getAllReports() {
        viewModelScope.launch(Dispatchers.IO) {
            getAllReportsUseCase.getReports().collect {

                when (it) {
                    is ResultState.Loading -> {
                        _reportState.value = CommonReportState(isLoading = true)
                    }

                    is ResultState.Success -> {
                        _reportState.value = CommonReportState(success = it.data, isLoading = false)
                    }

                    is ResultState.Error -> {
                        _reportState.value = CommonReportState(error = it.error, isLoading = false)
                    }
                }


            }

        }
    }

    fun makeReport(report: ReportModel) {

        viewModelScope.launch(Dispatchers.IO) {
            makeReportUseCase.makeReport(report).collect {

                when (it) {
                    is ResultState.Loading -> {
                        _makeReportState.value = CommonReportState(isLoading = true)
                    }

                    is ResultState.Success -> {
                        _makeReportState.value =
                            CommonReportState(success = it.data, isLoading = false)
                    }

                    is ResultState.Error -> {
                        _makeReportState.value =
                            CommonReportState(error = it.error, isLoading = false)
                    }
                }


            }
        }

    }

    fun deleteReport(reportId: String) {
        viewModelScope.launch(Dispatchers.IO) {

            deleteReportUseCase.deleteReport(reportId).collect {

                when (it) {
                    is ResultState.Loading -> {
                        _deleteReportState.value = CommonReportState(isLoading = true)
                    }

                    is ResultState.Success -> {
                        _deleteReportState.value =
                            CommonReportState(success = it.data, isLoading = false)
                    }

                    is ResultState.Error -> {
                        _deleteReportState.value =
                            CommonReportState(error = it.error, isLoading = false)
                    }
                }


            }


        }

    }

    fun updateReport(report: ReportModel) {
        viewModelScope.launch(Dispatchers.IO) {


            updateReportUseCase.updateReport(report).collect {

                when (it) {
                    is ResultState.Loading -> {
                        _updateReportState.value = CommonReportState(isLoading = true)
                    }

                    is ResultState.Success -> {
                        _updateReportState.value =
                            CommonReportState(success = it.data, isLoading = false)
                    }

                    is ResultState.Error -> {
                        _updateReportState.value =
                            CommonReportState(error = it.error, isLoading = false)
                    }
                }


            }

        }


    }


}

data class CommonReportState<T>(
    val isLoading: Boolean = false,
    val success: T? = null,
    val error: String = ""
)