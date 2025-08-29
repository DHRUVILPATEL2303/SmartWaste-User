package com.example.smartwaste_user.domain.repo.geminirepositry

import android.net.Uri
import com.example.smartwaste_user.common.ResultState
import kotlinx.coroutines.flow.Flow

interface GeminiRepository {
    suspend fun askTextQuestion(question: String): String
    suspend fun askImageQuestion(question: String, imageUri: Uri): String

    suspend fun askVideoQuestion(question : String, videoUri: Uri) : Flow<ResultState<String>>
}