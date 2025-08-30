package com.example.smartwaste_user.domain.repo.editorialrepo

import com.example.smartwaste_user.common.ResultState
import com.example.smartwaste_user.data.models.EdiorialModel
import kotlinx.coroutines.flow.Flow

interface EditorialRepositry {

    suspend fun getAllEditorials() : Flow<ResultState<List<EdiorialModel>>>
}