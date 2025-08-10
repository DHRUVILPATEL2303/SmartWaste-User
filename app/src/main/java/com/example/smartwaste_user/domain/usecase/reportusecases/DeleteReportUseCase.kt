package com.example.smartwaste_user.domain.usecase.reportusecases

import com.example.smartwaste_user.domain.repo.reportrepo.ReportRepositry
import javax.inject.Inject

class DeleteReportUseCase @Inject constructor(
    private val reportRepository: ReportRepositry
) {

    suspend fun deleteReport(reportId: String) = reportRepository.deleteReport(reportId)

}