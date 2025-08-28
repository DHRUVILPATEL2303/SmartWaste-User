package com.example.smartwaste_user.presentation.viewmodels.geminiviewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartwaste_user.domain.usecase.geminiusecase.GetResponseForImageUseCAse
import com.example.smartwaste_user.domain.usecase.geminiusecase.GetTextResponseUSeCAse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Represents a single message in the chat
data class Message(
    val text: String,
    val imageUri: Uri? = null,
    val isFromUser: Boolean
)

// Represents the entire state of the chat screen
data class ChatState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class GeminiViewModel @Inject constructor(
    private val getTextResponseUseCase: GetTextResponseUSeCAse,
    private val getResponseForImageUseCase: GetResponseForImageUseCAse
) : ViewModel() {

    private val _chatState = MutableStateFlow(ChatState())
    val chatState: StateFlow<ChatState> = _chatState.asStateFlow()

    init {
        // Add a welcome message from the bot
        _chatState.update {
            it.copy(
                messages = listOf(
                    Message("Hello! How can I help you with waste management today?", isFromUser = false)
                )
            )
        }
    }

    fun sendMessage(prompt: String) {

        _chatState.update {
            it.copy(
                messages = it.messages + Message(text = prompt, isFromUser = true),
                isLoading = true,
                error = null
            )
        }

        viewModelScope.launch {
            try {
                val response = getTextResponseUseCase.getTextReponse(prompt)
                // Add bot response
                _chatState.update {
                    it.copy(
                        messages = it.messages + Message(text = response, isFromUser = false),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _chatState.update {
                    it.copy(
                        error = e.message ?: "An unknown error occurred",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun sendImagePrompt(imageUri: Uri, prompt: String) {
        // Add user message with image to the list
        _chatState.update {
            it.copy(
                messages = it.messages + Message(text = prompt, imageUri = imageUri, isFromUser = true),
                isLoading = true,
                error = null
            )
        }

        viewModelScope.launch {
            try {
                val response = getResponseForImageUseCase.getREsponseforImageUSeCAse(prompt, imageUri)
                // Add bot response
                _chatState.update {
                    it.copy(
                        messages = it.messages + Message(text = response, isFromUser = false),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _chatState.update {
                    it.copy(
                        error = e.message ?: "An unknown error occurred",
                        isLoading = false
                    )
                }
            }
        }
    }
}
