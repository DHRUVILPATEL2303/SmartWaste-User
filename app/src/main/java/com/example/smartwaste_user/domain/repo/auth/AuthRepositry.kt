package com.example.smartwaste_user.domain.repo.auth

import com.example.smartwaste_user.common.ResultState
import com.example.smartwaste_user.data.models.UserModel
import com.google.firebase.auth.FirebaseUser

interface AuthRepositry {

    suspend fun signUpUserWithEmailAndPassword( password : String,userModel: UserModel):ResultState<FirebaseUser>
}