package com.example.smartwaste_user.data.repoimpl.holidayrepoimpl

import com.example.smartwaste_user.common.HOLIDAY_PATH
import com.example.smartwaste_user.common.ResultState
import com.example.smartwaste_user.data.models.HolidayModel
import com.example.smartwaste_user.domain.repo.holidayrepo.HolidayRepositry
import com.example.smartwaste_user.presentation.screens.reportscreens.ReportScreenUI
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class HolidayRepositryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore
) : HolidayRepositry{
    override suspend fun getAllHolidays(): Flow<ResultState<List<HolidayModel>>> = callbackFlow{
            trySend(ResultState.Loading)

        try {
            val data=firebaseFirestore.collection(HOLIDAY_PATH).get().await()


            val holidays=data.toObjects(HolidayModel::class.java)
            trySend(ResultState.Success(holidays))
        }catch (e: Exception){
            trySend(ResultState.Error(e.message?: "Something error"))
        }

        awaitClose {
            close()
        }
    }

}