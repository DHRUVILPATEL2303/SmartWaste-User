package com.example.smartwaste_user.domain.usecase.userusecase

import com.example.smartwaste_user.domain.repo.userrepo.UserRepositry
import javax.inject.Inject

class GetUserDetailsUseCase @Inject constructor(
    private val userRepositry: UserRepositry
){

    suspend fun getUserDetailsUseCase()=userRepositry.getUserData()
}