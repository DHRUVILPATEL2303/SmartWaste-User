package com.example.smartwaste_user.domain.usecase.routeprogresusecase

import com.example.smartwaste_user.domain.repo.routeprogressmodel.RouteProgressRepositry
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class GetAllRoutesProgresssUseCase @Inject constructor(
    private val routeProgressRepositry: RouteProgressRepositry
) {

    suspend fun getAllRoutesProgressUseCase()=routeProgressRepositry.getAllRoutesProgress()
}