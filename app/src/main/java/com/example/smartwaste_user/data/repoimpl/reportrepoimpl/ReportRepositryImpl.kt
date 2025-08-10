package com.example.smartwaste_user.data.repoimpl.reportrepoimpl

import com.example.smartwaste_user.common.REPORTS_PATH
import com.example.smartwaste_user.common.ResultState
import com.example.smartwaste_user.data.models.ReportModel
import com.example.smartwaste_user.domain.repo.reportrepo.ReportRepositry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ReportRepositryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : ReportRepositry {
    override suspend fun getAllReportOfUser(): Flow<ResultState<List<ReportModel>>> = callbackFlow {

        trySend(ResultState.Loading)

        try {
            firebaseFirestore.collection(REPORTS_PATH)
                .whereEqualTo("userId", firebaseAuth.currentUser?.uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(ResultState.Error(error.message.toString()))
                    } else {
                        val reports = snapshot?.mapNotNull {
                            it.toObject(ReportModel::class.java)
                                .apply {
                                    reportId = it.id
                                }
                        }
                        if (reports != null) {
                            trySend(ResultState.Success(reports))
                        } else {
                            trySend(ResultState.Error("No reports found"))
                        }
                    }
                }
        } catch (e: Exception) {
            trySend(ResultState.Error(e.message.toString()))
        }
        awaitClose {
            close()
        }


    }

    override suspend fun makeReport(report: ReportModel): Flow<ResultState<String>> = callbackFlow {
        trySend(ResultState.Loading)

        try {
            firebaseFirestore.collection(REPORTS_PATH).document().set(report).await()

            trySend(ResultState.Success("Report made successfully"))
        } catch (e: Exception) {
            trySend(ResultState.Error(e.message.toString()))
        }
        awaitClose {
            close()
        }


    }

    override suspend fun deleteReport(reportId: String): Flow<ResultState<String>> = callbackFlow {

        trySend(ResultState.Loading)

        try {
            firebaseFirestore.collection(REPORTS_PATH).document(reportId).delete().await()

            trySend(ResultState.Success("Report deleted successfully"))
        } catch (e: Exception) {
            trySend(ResultState.Error(e.message.toString()))
        }
        awaitClose {
            close()
        }


    }

    override suspend fun updateReport(report: ReportModel): Flow<ResultState<String>> =
        callbackFlow {

            trySend(ResultState.Loading)

            try {
                firebaseFirestore.collection(REPORTS_PATH).document(report.reportId).set(report)
                    .await()

                trySend(ResultState.Success("Report updated successfully"))
            } catch (e: Exception) {
                trySend(ResultState.Error(e.message.toString()))
            }
            awaitClose {
                close()
            }


        }
}