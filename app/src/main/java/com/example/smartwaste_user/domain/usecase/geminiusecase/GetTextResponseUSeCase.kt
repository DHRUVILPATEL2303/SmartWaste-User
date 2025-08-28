package com.example.smartwaste_user.domain.usecase.geminiusecase

import com.example.smartwaste_user.domain.repo.geminirepositry.GeminiRepository
import javax.inject.Inject

class GetTextResponseUSeCAse  @Inject
constructor(
    private val geminiRepository: GeminiRepository
){
    suspend fun getTextReponse(question :String)=geminiRepository.askTextQuestion(question)
}