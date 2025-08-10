package com.example.smartwaste_user.data.repoimpl.userrepoimpl

import com.example.smartwaste_user.common.ResultState
import com.example.smartwaste_user.common.USERS_PATH
import com.example.smartwaste_user.data.di.DataModule_ProvideFirebaseAuthFactory
import com.example.smartwaste_user.data.models.UserModel
import com.example.smartwaste_user.domain.repo.userrepo.UserRepositry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : UserRepositry {
    override suspend fun getUserData(): Flow<ResultState<UserModel>> = callbackFlow {

        trySend(ResultState.Loading)


        try {
            firebaseFirestore.collection(USERS_PATH).document(firebaseAuth.currentUser!!.uid)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        trySend(ResultState.Error(error.message ?: "Unknown error occurred"))
                        return@addSnapshotListener
                    }
                    val userModel = value?.toObject(UserModel::class.java)
                    trySend(ResultState.Success(userModel!!))
                }

        } catch (e: Exception) {
            trySend(ResultState.Error(e.message ?: "Unknown error occurred"))

        }

        awaitClose {
            close()
        }
    }

    override suspend fun updateUserData(userModel: UserModel): ResultState<String> {

        try {

            firebaseFirestore.collection(USERS_PATH).document(firebaseAuth.currentUser!!.uid).set(userModel).await()
            return ResultState.Success("User data updated successfully")

        }catch (e: Exception){
            return ResultState.Error(e.message ?: "Unknown error occurred")

        }
    }
}