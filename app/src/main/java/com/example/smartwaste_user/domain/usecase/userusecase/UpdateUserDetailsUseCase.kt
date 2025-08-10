package com.example.smartwaste_user.domain.usecase.userusecase

import com.example.smartwaste_user.data.models.UserModel
import com.example.smartwaste_user.domain.repo.userrepo.UserRepositry
import javax.inject.Inject

class UpdateUserDetailsUseCase @Inject constructor(
    private val userRepositry: UserRepositry
) {

    suspend fun updateUserDetailsUseCase(userModel: UserModel) = userRepositry.updateUserData(userModel)
}