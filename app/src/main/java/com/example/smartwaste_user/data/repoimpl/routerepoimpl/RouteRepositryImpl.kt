package com.example.smartwaste_user.data.repoimpl.routerepoimpl

import com.example.smartwaste_user.common.ResultState
import com.example.smartwaste_user.data.models.RouteModel
import com.example.smartwaste_user.domain.repo.routerepo.RouteRepositry
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class RouteRepositryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : RouteRepositry {
    override suspend fun getAllRoutes(): Flow<ResultState<List<RouteModel>>> = callbackFlow {
        trySend(ResultState.Loading)

        try {
            firestore.collection("routes").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(ResultState.Error(error.message ?: "Unknown error"))
                }
                if (snapshot != null) {
                    val routes = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(RouteModel::class.java)
                    }
                    trySend(ResultState.Success(routes))
                }
            }

        } catch (e: Exception) {
            trySend(ResultState.Error(e.message ?: "Unknown error"))

        }

        awaitClose {
            close()
        }
    }
}

