package com.example.smartwaste_user.domain.usecase.geminiusecase

import android.net.Uri
import com.example.smartwaste_user.domain.repo.geminirepositry.GeminiRepository
import javax.inject.Inject

class GetResponseForImageUseCAse @Inject constructor(
    private val geminiRepository: GeminiRepository
) {
    suspend fun getREsponseforImageUSeCAse(
        question :String,
        imageurl : Uri
    )=geminiRepository.askImageQuestion(question = question, imageUri = imageurl)
}