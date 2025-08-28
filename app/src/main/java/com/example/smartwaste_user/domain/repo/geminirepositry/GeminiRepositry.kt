package com.example.smartwaste_user.domain.repo.geminirepositry

import android.net.Uri

interface GeminiRepository {
    suspend fun askTextQuestion(question: String): String
    suspend fun askImageQuestion(question: String, imageUri: Uri): String
}