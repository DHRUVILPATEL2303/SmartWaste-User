package com.example.smartwaste_user.data.repoimpl.geminirepoimpl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import com.example.smartwaste_user.domain.repo.geminirepositry.GeminiRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GeminiRepositoryImpl @Inject constructor(
    private val generativeModel: GenerativeModel,
    @ApplicationContext private val context: Context,
) : GeminiRepository {


    override suspend fun askTextQuestion(question: String): String =
        withContext(Dispatchers.IO) {
            val modifiedPrompt = """
            You are an assistant and give short answer and user friendly . 
            If the following question is about **waste** (garbage collection, recycling, smart waste, plastic,garbage,every type of waste), 
            then answer it properly. 


            Question: $question
        """.trimIndent()

            val response = generativeModel.generateContent(modifiedPrompt)
            response.text ?: "No response from AI"
        }


    override suspend fun askImageQuestion(question: String, imageUri: Uri): String =
        withContext(Dispatchers.IO) {
            try {
                val bitmap = uriToBitmap(imageUri)
                val modifiedPrompt = """
            You are an assistant and give short answer and user friendly . 
            If the following question is about **waste** (garbage collection, recycling, smart waste, plastic,garbage,every type of waste), 
            then answer it properly. 


            Question: $question
        """.trimIndent()
                val response = generativeModel.generateContent(
                    content {
                        image(bitmap)
                        text(modifiedPrompt)
                    }
                )
                response.text ?: "No response from AI"
            } catch (e: Exception) {
                e.printStackTrace()
                "Error: ${e.message}"
            }
        }

    private fun uriToBitmap(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT < 28) {
            @Suppress("DEPRECATION")
            android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        } else {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        }
    }
}