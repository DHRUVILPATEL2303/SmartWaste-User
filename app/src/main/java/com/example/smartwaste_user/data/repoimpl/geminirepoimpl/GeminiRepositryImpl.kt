package com.example.smartwaste_user.data.repoimpl.geminirepoimpl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import com.example.smartwaste_user.common.ResultState
import com.example.smartwaste_user.domain.repo.geminirepositry.GeminiRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.content

import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GeminiRepositoryImpl @Inject constructor(
    private val generativeModel: GenerativeModel,
    @ApplicationContext private val context: Context,
) : GeminiRepository {


    override suspend fun askTextQuestion(question: String): Flow<ResultState<String>> = flow {
        val modifiedPrompt = """
        You are an assistant and give short, user-friendly answers. 
        If the following question is about **waste** (garbage collection, recycling, smart waste, plastic, garbage, etc.), 
        then answer it properly.

        Question: $question
    """.trimIndent()


        generativeModel.generateContentStream(modifiedPrompt).collect { chunk: GenerateContentResponse ->
            chunk.text?.let { emit(ResultState.Success(it)) }
        }
    }




    override suspend fun askImageQuestion(
        question: String,
        imageUri: Uri
    ): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)

        try {
            val bitmap = uriToBitmap(imageUri)
            val modifiedPrompt = """
            You are an assistant and give short, user-friendly answers. 
            If the following question is about **waste** (garbage collection, recycling, smart waste, plastic, garbage, every type of waste), 
            then answer it properly. 

            Question: $question
        """.trimIndent()

            val builder = StringBuilder()

            generativeModel.generateContentStream(
                content {
                    image(bitmap)
                    text(modifiedPrompt)
                }
            ).collect { chunk ->
                val partial = chunk.text
                if (!partial.isNullOrBlank()) {
                    builder.append(partial)
                    emit(ResultState.Success(builder.toString()))
                }
            }

        } catch (e: Exception) {
            emit(ResultState.Error(e.message ?: "Error processing image"))
        }
    }

    override suspend fun askVideoQuestion(
        question: String,
        videoUri: Uri
    ): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)

        val modifiedPrompt = """
            You are an assistant and give short answer and user friendly  and reply answer as the images are video and i am providing you the video. 
            If the following question is about **waste** (garbage collection, recycling, smart waste, plastic,garbage,every type of waste), 
            then answer it properly . 


            Question: $question
        """.trimIndent()

        try {
            val frames = extractFramesFromVideo(context, videoUri)

            val responses = mutableListOf<String>()


            for ((index, frame) in frames.withIndex()) {
                val response = generativeModel.generateContent(
                    content {
                        image(frame)
                        text("$modifiedPrompt (Frame $index)")
                    }
                )
                responses.add(response.text ?: "")
            }

            val finalSummary = responses.joinToString("\n")
            emit(ResultState.Success(finalSummary))

        } catch (e: Exception) {
            emit(ResultState.Error(e.message ?: "Error processing video"))
        }
    }

    fun extractFramesFromVideo(context: Context, videoUri: Uri, intervalMs: Long = 2000): List<Bitmap> {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, videoUri)

        val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val durationMs = durationStr?.toLongOrNull() ?: 0L

        val frames = mutableListOf<Bitmap>()
        var timeMs = 0L

        while (timeMs < durationMs) {
            retriever.getFrameAtTime(timeMs * 1000, MediaMetadataRetriever.OPTION_CLOSEST)?.let {
                frames.add(it)
            }
            timeMs += intervalMs
        }
        retriever.release()
        return frames
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


