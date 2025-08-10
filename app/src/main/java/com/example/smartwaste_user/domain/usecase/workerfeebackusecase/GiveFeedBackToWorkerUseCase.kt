package com.example.smartwaste_user.domain.usecase.workerfeebackusecase

import com.example.smartwaste_user.data.models.WorkerFeedBackModel
import com.example.smartwaste_user.domain.repo.workerfedbackrepo.WorkerFeedBackRepositry
import javax.inject.Inject

class GiveFeedBackToWorkerUseCase @Inject constructor(
    private val workerFeedBackRepositry: WorkerFeedBackRepositry
) {

    suspend fun giveFeedBacktoWorker(feedBackModel: WorkerFeedBackModel)=workerFeedBackRepositry.giveFeedBacktoWorker(feedBackModel)
}