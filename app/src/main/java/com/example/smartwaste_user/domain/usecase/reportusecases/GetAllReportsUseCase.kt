package com.example.smartwaste_user.domain.usecase.reportusecases

import com.example.smartwaste_user.domain.repo.reportrepo.ReportRepositry
import javax.inject.Inject

class GetAllReportsUseCase @Inject constructor(
    private val reportRepositry: ReportRepositry
) {

    suspend fun getReports() = reportRepositry.getAllReportOfUser()

}