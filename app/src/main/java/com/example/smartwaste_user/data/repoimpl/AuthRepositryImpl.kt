package com.example.smartwaste_user.data.repoimpl

import com.example.smartwaste_user.common.FCM_PATH
import com.example.smartwaste_user.common.ResultState
import com.example.smartwaste_user.common.USERS_PATH
import com.example.smartwaste_user.common.FCM_PATH
import com.example.smartwaste_user.data.models.UserModel
import com.example.smartwaste_user.domain.repo.auth.AuthRepositry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseMessaging: FirebaseMessaging
) : AuthRepositry {

    private suspend fun storeFcmToken(userId: String) {
        try {
            val token = firebaseMessaging.token.await()
            firebaseFirestore.collection(FCM_PATH)
                .document(userId)
                .set(mapOf("fcm_token" to token))
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun signUpUserWithEmailAndPassword(
        password: String,
        userModel: UserModel
    ): ResultState<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(userModel.email, password).await()
            val user = result.user

            user?.let {
                it.sendEmailVerification().await()

                userModel.userId = it.uid

                firebaseFirestore.collection(USERS_PATH)
                    .document(it.uid)
                    .set(userModel)
                    .await()

                storeFcmToken(it.uid)

                ResultState.Success(it)
            } ?: ResultState.Error("User creation failed")
        } catch (e: Exception) {
            ResultState.Error(e.localizedMessage ?: "Unknown error occurred")
        }
    }

    override suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): ResultState<FirebaseUser> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = firebaseAuth.currentUser

            return if (user?.isEmailVerified == true) {
                storeFcmToken(user.uid)
                ResultState.Success(user)
            } else {
                ResultState.Error("Please verify your email before logging in.")
            }
        } catch (e: Exception) {
            ResultState.Error(e.localizedMessage ?: "Unknown error occurred")
        }
    }

    override suspend fun firebaseSignInWithGoogle(idToken: String): ResultState<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: return ResultState.Error("Google sign-in failed")

            val userDocRef = firebaseFirestore.collection(USERS_PATH).document(firebaseUser.uid)
            val snapshot = userDocRef.get().await()

            if (!snapshot.exists()) {
                val userModel = UserModel(
                    userId = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: "",
                    profileImageUrl = firebaseUser.photoUrl?.toString() ?: "",
                    phoneNumber = firebaseUser.phoneNumber?.toString() ?: "",
                    address = "",
                    areaName = "",
                    routeName = "",
                    totalPoints = "0",
                    routeId = ""
                )
                userDocRef.set(userModel).await()
            }

            storeFcmToken(firebaseUser.uid)

            ResultState.Success(firebaseUser)
        } catch (e: Exception) {
            ResultState.Error(e.localizedMessage ?: "Unknown error occurred")
        }
    }
}