package com.example.smartwaste_user.data.repoimpl.extraservicerepoimpl

import com.example.smartwaste_user.common.EXTRA_SERVICE_PATH
import com.example.smartwaste_user.common.ResultState
import com.example.smartwaste_user.data.models.ExtraServiceModel
import com.example.smartwaste_user.domain.repo.extraservice.ExtraServiceRepositry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ExtraServiceRepositryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
): ExtraServiceRepositry {
    override suspend fun requestExtraService(extraServiceModel: ExtraServiceModel): Flow<ResultState<String>> =
        flow {
            emit(ResultState.Loading)

            try {
                val userId = firebaseAuth.currentUser!!.uid
                val requestId = firebaseFirestore.collection(EXTRA_SERVICE_PATH)
                    .document(userId)
                    .collection("extra_services")
                    .document().id

                firebaseFirestore.collection(EXTRA_SERVICE_PATH)
                    .document(userId)
                    .collection("extra_services")
                    .document(requestId)
                    .set(extraServiceModel.copy(id = requestId, userId = userId))
                    .await()

                emit(ResultState.Success("Extra service request sent successfully"))
            } catch (e: Exception) {
                emit(ResultState.Error(e.message.toString()))
            }
        }

    override suspend fun deleteExtraService(id: String): Flow<ResultState<String>> =
        flow {
            emit(ResultState.Loading)

            try {
                val userId = firebaseAuth.currentUser!!.uid
                firebaseFirestore.collection(EXTRA_SERVICE_PATH)
                    .document(userId)
                    .collection("extra_services")
                    .document(id)
                    .delete()
                    .await()

                emit(ResultState.Success("Extra service request deleted successfully"))
            } catch (e: Exception) {
                emit(ResultState.Error(e.message.toString()))
            }
        }

    override suspend fun getAllExtraServices(): Flow<ResultState<List<ExtraServiceModel>>> =
        callbackFlow {
            trySend(ResultState.Loading)

            val userId = firebaseAuth.currentUser!!.uid
            val listener = firebaseFirestore.collection(EXTRA_SERVICE_PATH)
                .document(userId)
                .collection("extra_services")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        trySend(ResultState.Error(e.message.toString()))
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val services = snapshot.toObjects(ExtraServiceModel::class.java)
                        trySend(ResultState.Success(services))
                    }
                }

            awaitClose { listener.remove() }
        }


}