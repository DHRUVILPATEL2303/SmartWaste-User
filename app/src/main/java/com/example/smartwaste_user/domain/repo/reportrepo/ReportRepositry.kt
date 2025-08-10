package com.example.smartwaste_user.domain.repo.reportrepo

import com.example.smartwaste_user.common.ResultState
import com.example.smartwaste_user.data.models.ReportModel
import kotlinx.coroutines.flow.Flow

interface ReportRepositry {

    suspend fun getAllReportOfUser(): Flow<ResultState<List<ReportModel>>>

    suspend fun makeReport(report: ReportModel): Flow<ResultState<String>>

    suspend fun deleteReport(reportId: String): Flow<ResultState<String>>

    suspend fun updateReport(report: ReportModel) : Flow<ResultState<String>>
}