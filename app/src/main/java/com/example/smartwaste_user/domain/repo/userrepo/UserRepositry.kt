package com.example.smartwaste_user.domain.repo.userrepo

import com.example.smartwaste_user.common.ResultState
import com.example.smartwaste_user.data.models.UserModel
import kotlinx.coroutines.flow.Flow

interface UserRepositry {

    suspend fun getUserData() : Flow<ResultState<UserModel>>

    suspend fun updateUserData(userModel: UserModel) : ResultState<String>
}