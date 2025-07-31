package com.example.smartwaste_user.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartwaste_user.common.ResultState
import com.example.smartwaste_user.data.models.UserModel
import com.example.smartwaste_user.domain.usecase.authusecases.SignUpUserUseCase
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signUpUserUseCase: SignUpUserUseCase
) : ViewModel() {

    private val _signUpState = MutableStateFlow(Common())
    val signUpState = _signUpState.asStateFlow()

    fun signUpUser(password: String, userModel: UserModel) {
        viewModelScope.launch(Dispatchers.IO) {
            _signUpState.value = Common(isLoading = true)
            Log.d("AuthViewModel", "Attempting signup for email: ${userModel.email}")

            when (val result = signUpUserUseCase.signUpUserUseCase(password, userModel)) {
                is ResultState.Success -> {
                    Log.d("AuthViewModel", "Signup successful: ${result.data?.email}")
                    _signUpState.value = Common(success = result.data)
                }
                is ResultState.Error -> {
                    Log.e("AuthViewModel", "Signup failed: ${result.error}")
                    _signUpState.value = Common(error = result.error)
                }
                else -> {
                    Log.w("AuthViewModel", "Unexpected result state: $result")
                    _signUpState.value = Common(error = "Unexpected error occurred")
                }
            }
            _signUpState.value = _signUpState.value.copy(isLoading = false)
        }
    }

    fun setError(error: String) {
        _signUpState.value = Common(error = error)
    }
}

data class Common(
    val isLoading: Boolean = false,
    val success: FirebaseUser? = null,
    val error: String = ""
)