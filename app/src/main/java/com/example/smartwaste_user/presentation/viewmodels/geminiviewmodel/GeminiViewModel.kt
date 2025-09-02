package com.example.smartwaste_user.presentation.viewmodels.geminiviewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartwaste_user.common.ResultState
import com.example.smartwaste_user.domain.usecase.geminiusecase.GetResponseForImageUseCAse
import com.example.smartwaste_user.domain.usecase.geminiusecase.GetTextResponseUSeCAse
import com.example.smartwaste_user.domain.usecase.geminiusecase.GetVideoResponseUSecAse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Message(
    val text: String,
    val imageUri: Uri? = null,
    val videoUri: Uri? = null,
    val isFromUser: Boolean
)

data class ChatState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class GeminiViewModel @Inject constructor(
    private val application: Application,
    private val getTextResponseUseCase: GetTextResponseUSeCAse,
    private val getResponseForImageUseCase: GetResponseForImageUseCAse,
    private val getVideoResponseUSecAse: GetVideoResponseUSecAse
) : ViewModel() {

    private val _chatState = MutableStateFlow(ChatState())
    val chatState: StateFlow<ChatState> = _chatState.asStateFlow()

    init {
        _chatState.update {
            it.copy(
                messages = listOf(
                    Message("Hello! I am SmartWaste Assistant. How can I help you with waste management today? 🌱", isFromUser = false)
                )
            )
        }
    }

    fun sendMessage(prompt: String, uri: Uri? = null) {
        val context = application.applicationContext

        val userMessage = Message(
            text = prompt,
            isFromUser = true,
            imageUri = if (isImage(context, uri)) uri else null,
            videoUri = if (isVideo(context, uri)) uri else null
        )
        _chatState.update {
            it.copy(
                messages = it.messages + userMessage,
                isLoading = true,
                error = null
            )
        }

        viewModelScope.launch {
            try {
                when {
                    isVideo(context, uri) -> {
                        handleVideoPrompt(uri!!, prompt)
                    }
                    isImage(context, uri) -> {
                        handleImagePrompt(uri!!, prompt)
                    }
                    else -> {
                        handleTextPrompt(prompt)
                    }
                }
            } catch (e: Exception) {
                _chatState.update {
                    it.copy(
                        error = e.message ?: "An unexpected error occurred.",
                        isLoading = false
                    )
                }
            }
        }
    }


    private suspend fun handleTextPrompt(prompt: String) {
        getTextResponseUseCase.getTextReponse(prompt).collect { result ->
            when (result) {
                is ResultState.Success -> {
                    _chatState.update { state ->
                        val updatedMessages = state.messages.toMutableList()
                        if (updatedMessages.isNotEmpty() && !updatedMessages.last().isFromUser) {
                            // update last bot message
                            val last = updatedMessages.removeAt(updatedMessages.lastIndex)
                            updatedMessages.add(last.copy(text = result.data))
                        } else {
                            // first bot response
                            updatedMessages.add(Message(text = result.data, isFromUser = false))
                        }
                        state.copy(messages = updatedMessages, isLoading = false)
                    }
                }
                is ResultState.Error -> {
                    _chatState.update {
                        it.copy(error = result.error, isLoading = false)
                    }
                }
                is ResultState.Loading -> {
                }
            }
        }
    }

    private suspend fun handleImagePrompt(imageUri: Uri, prompt: String) {
        val finalPrompt = prompt.ifBlank { "Analyze this waste image and provide recycling guidance." }
        getResponseForImageUseCase.getREsponseforImageUSeCAse(finalPrompt, imageUri).collect { result ->
            when (result) {
                is ResultState.Success -> {
                    _chatState.update { state ->
                        val updatedMessages = state.messages.toMutableList()
                        if (updatedMessages.isNotEmpty() && !updatedMessages.last().isFromUser) {
                            val last = updatedMessages.removeAt(updatedMessages.lastIndex)
                            updatedMessages.add(last.copy(text = result.data))
                        } else {
                            updatedMessages.add(Message(text = result.data, isFromUser = false))
                        }
                        state.copy(messages = updatedMessages, isLoading = false)
                    }
                }
                is ResultState.Error -> {
                    _chatState.update {
                        it.copy(error = result.error, isLoading = false)
                    }
                }
                is ResultState.Loading -> { }
            }
        }
    }

    private suspend fun handleVideoPrompt(videoUri: Uri, prompt: String) {
        val finalPrompt = prompt.ifBlank { "Analyze this waste video and provide recycling guidance." }
        getVideoResponseUSecAse.getVideoResponse(videoUri, finalPrompt).collect { result ->
            when (result) {
                is ResultState.Loading -> {
                }
                is ResultState.Success -> {
                    _chatState.update { state ->
                        state.copy(
                            messages = state.messages + Message(
                                text = result.data,
                                isFromUser = false
                            ),
                            isLoading = false
                        )
                    }
                }
                is ResultState.Error -> {
                    _chatState.update { state ->
                        state.copy(
                            error = result.error,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }



    private fun getMimeType(context: Context, uri: Uri): String? {
        return context.contentResolver.getType(uri)
    }

    private fun isImage(context: Context, uri: Uri?): Boolean {
        if (uri == null) return false
        val mimeType = getMimeType(context, uri)
        return mimeType?.startsWith("image/") == true
    }

    private fun isVideo(context: Context, uri: Uri?): Boolean {
        if (uri == null) return false
        val mimeType = getMimeType(context, uri)
        return mimeType?.startsWith("video/") == true
    }
}