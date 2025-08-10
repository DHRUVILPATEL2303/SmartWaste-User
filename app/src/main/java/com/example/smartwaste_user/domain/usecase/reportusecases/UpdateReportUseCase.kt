package com.example.smartwaste_user.domain.usecase.reportusecases

import com.example.smartwaste_user.data.models.ReportModel
import com.example.smartwaste_user.domain.repo.reportrepo.ReportRepositry
import javax.inject.Inject

class UpdateReportUseCase @Inject constructor(
    private val reportRepository: ReportRepositry
) {
    suspend fun updateReport(report: ReportModel) = reportRepository.updateReport(report)
}