package com.example.smartwaste_user.domain.repo.workerfedbackrepo

import com.example.smartwaste_user.common.ResultState
import com.example.smartwaste_user.data.models.RouteProgressModel
import com.example.smartwaste_user.data.models.WorkerFeedBackModel
import kotlinx.coroutines.flow.Flow

interface WorkerFeedBackRepositry {

    suspend fun giveFeedBacktoWorker(feedBackModel: WorkerFeedBackModel) : Flow<ResultState<String>>
}