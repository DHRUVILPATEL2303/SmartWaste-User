package com.example.smartwaste_user.data.repoimpl.editorialrepoimpl

import com.example.smartwaste_user.common.EDITORIAL_PATH
import com.example.smartwaste_user.common.ResultState
import com.example.smartwaste_user.data.models.EdiorialModel
import com.example.smartwaste_user.domain.repo.editorialrepo.EditorialRepositry
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class EditorialRepositryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : EditorialRepositry{
    override suspend fun getAllEditorials(): Flow<ResultState<List<EdiorialModel>>> = callbackFlow{
        trySend(ResultState.Loading)
        try {
            firestore.collection(EDITORIAL_PATH).addSnapshotListener {snapshot,e->
                if (e!=null){
                    trySend(ResultState.Error(e.localizedMessage.toString()))
                }

                val data=snapshot!!.documents.mapNotNull {
                    it.toObject(EdiorialModel::class.java)
                }

                trySend(ResultState.Success(data))
            }
        }catch (e: Exception){
            trySend(ResultState.Error(e.localizedMessage.toString()))
        }

        awaitClose { close() }
    }

}