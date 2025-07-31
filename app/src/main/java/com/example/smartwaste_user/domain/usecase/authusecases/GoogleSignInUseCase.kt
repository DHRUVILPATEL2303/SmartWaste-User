package com.example.smartwaste_user.domain.usecase.authusecases

import com.example.smartwaste_user.domain.repo.auth.AuthRepositry
import javax.inject.Inject


class SignInWithGoogleUseCase @Inject constructor(
    private val repository: AuthRepositry
) {
    suspend operator fun invoke(idToken: String) = repository.firebaseSignInWithGoogle(idToken)
}