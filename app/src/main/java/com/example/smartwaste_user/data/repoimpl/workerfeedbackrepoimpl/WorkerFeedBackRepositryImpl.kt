package com.example.smartwaste_user.data.repoimpl.workerfeedbackrepoimpl

import com.example.smartwaste_user.common.ResultState
import com.example.smartwaste_user.common.WORKER_FEEDBACK
import com.example.smartwaste_user.data.models.WorkerFeedBackModel
import com.example.smartwaste_user.domain.repo.workerfedbackrepo.WorkerFeedBackRepositry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class WorkerFeedBackRepositryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : WorkerFeedBackRepositry {

    override suspend fun giveFeedBacktoWorker(feedBackModel: WorkerFeedBackModel): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)

        try {
            firebaseFirestore.collection(WORKER_FEEDBACK)
                .document()
                .set(feedBackModel)
                .await()

            emit(ResultState.Success("Feedback submitted successfully"))
        } catch (e: Exception) {
            emit(ResultState.Error(e.message.toString()))
        }
    }
}