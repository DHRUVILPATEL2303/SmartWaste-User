package com.example.smartwaste_user.domain.usecase.authusecases

import com.example.smartwaste_user.data.models.UserModel
import com.example.smartwaste_user.domain.repo.auth.AuthRepositry
import javax.inject.Inject

class SignUpUserUseCase @Inject constructor(
    private val authRepositry: AuthRepositry
) {

    suspend fun signUpUserUseCase(password : String, userModel: UserModel) = authRepositry.signUpUserWithEmailAndPassword(password,userModel)
}