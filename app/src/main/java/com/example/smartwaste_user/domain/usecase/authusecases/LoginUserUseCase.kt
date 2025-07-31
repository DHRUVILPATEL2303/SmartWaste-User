package com.example.smartwaste_user.domain.usecase.authusecases

import com.example.smartwaste_user.domain.repo.auth.AuthRepositry
import javax.inject.Inject

class LoginUserUseCase @Inject constructor(
    private val authRepositry: AuthRepositry
) {

    suspend fun signWithEmailAndPassword(email : String,password: String)=authRepositry.signInWithEmailAndPassword(email,password)
}