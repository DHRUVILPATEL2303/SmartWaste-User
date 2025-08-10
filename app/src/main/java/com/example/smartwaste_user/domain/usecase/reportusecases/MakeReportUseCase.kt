package com.example.smartwaste_user.domain.usecase.reportusecases

import com.example.smartwaste_user.data.models.ReportModel
import com.example.smartwaste_user.domain.repo.reportrepo.ReportRepositry
import javax.inject.Inject

class MakeReportUseCase @Inject constructor(
    private val reportRepositry: ReportRepositry
) {

    suspend fun makeReport(reportModel: ReportModel)=reportRepositry.makeReport(reportModel)
}