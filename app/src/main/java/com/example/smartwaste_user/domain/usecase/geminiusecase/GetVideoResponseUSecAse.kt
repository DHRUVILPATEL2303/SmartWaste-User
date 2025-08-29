package com.example.smartwaste_user.domain.usecase.geminiusecase

import android.net.Uri
import com.example.smartwaste_user.domain.repo.geminirepositry.GeminiRepository
import javax.inject.Inject

class GetVideoResponseUSecAse @Inject constructor(
    private val geminiRepository: GeminiRepository
) {

    suspend fun getVideoResponse(videoUri : Uri,question : String)=geminiRepository.askVideoQuestion(question,videoUri)
}