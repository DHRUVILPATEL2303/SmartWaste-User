package com.example.smartwaste_user.data.repoimpl

import com.example.smartwaste_user.common.ResultState
import com.example.smartwaste_user.data.models.UserModel
import com.example.smartwaste_user.domain.repo.auth.AuthRepositry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore
) : AuthRepositry {

    override suspend fun signUpUserWithEmailAndPassword(
        password: String,
        userModel: UserModel
    ): ResultState<FirebaseUser> {
        return try {
            firebaseAuth.createUserWithEmailAndPassword(userModel.email, password).await()


            userModel.userId = firebaseAuth.currentUser!!.uid


            firebaseFirestore.collection("users")
                .document(firebaseAuth.currentUser!!.uid)
                .set(userModel)
                .await()

            ResultState.Success(firebaseAuth.currentUser!!)
        } catch (e: Exception) {
            ResultState.Error(e.localizedMessage ?: "Unknown error occurred")
        }
    }

    override suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): ResultState<FirebaseUser> {

        return  try {
            firebaseAuth.signInWithEmailAndPassword(email,password).await()
            ResultState.Success(firebaseAuth.currentUser!!)
        }catch (e: Exception){
            ResultState.Error(e.localizedMessage!!)
        }
    }

    override suspend fun firebaseSignInWithGoogle(idToken: String): ResultState<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            firebaseAuth.signInWithCredential(credential).await()
            ResultState.Success(firebaseAuth.currentUser!!)

        } catch (e: Exception) {
            ResultState.Error(e.localizedMessage.toString())
        }
    }
}