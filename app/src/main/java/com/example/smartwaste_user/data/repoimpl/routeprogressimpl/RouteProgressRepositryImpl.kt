package com.example.smartwaste_user.data.repoimpl.routeprogressimpl

import com.example.smartwaste_user.common.ROUTE_PROGRESS_PATH
import com.example.smartwaste_user.common.ResultState
import com.example.smartwaste_user.data.models.RouteProgressModel
import com.example.smartwaste_user.domain.repo.routeprogressmodel.RouteProgressRepositry
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class RouteProgressRepositryImpl @Inject constructor(
    private val firestoreFirestore: FirebaseFirestore
) : RouteProgressRepositry {
    override suspend fun getAllRoutesProgress(): Flow<ResultState<List<RouteProgressModel>>> =
        callbackFlow{


            trySend(ResultState.Loading)

            try {
                firestoreFirestore.collection(ROUTE_PROGRESS_PATH).addSnapshotListener { value, error ->

                    if (error != null) {
                        trySend(ResultState.Error(error.message ?: "Unknown error occurred"))
                        return@addSnapshotListener

                    }

                    val routeProgressList = value?.toObjects(RouteProgressModel::class.java) ?: emptyList()
                    trySend(ResultState.Success(routeProgressList))
                }



            }catch (e: Exception){
                trySend(ResultState.Error(e.message ?: "Unknown error occurred"))

            }

            awaitClose {
                close()
            }
        }


}