package com.example.smartwaste_user.domain.usecase.ediorialusecase

import com.example.smartwaste_user.domain.repo.editorialrepo.EditorialRepositry
import javax.inject.Inject

class GetAllEditorialUseCase @Inject constructor(
    private val editorialRepositry: EditorialRepositry
) {

    suspend fun getAllEditorials()=editorialRepositry.getAllEditorials()
}