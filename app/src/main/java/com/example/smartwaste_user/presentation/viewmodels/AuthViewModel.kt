package com.example.smartwaste_user.presentation.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartwaste_user.common.ResultState
import com.example.smartwaste_user.data.models.UserModel
import com.example.smartwaste_user.domain.usecase.authusecases.LoginUserUseCase

import com.example.smartwaste_user.domain.usecase.authusecases.SignInWithGoogleUseCase
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
    private val signUpUserUseCase: SignUpUserUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val loginUserUseCase: LoginUserUseCase
) : ViewModel() {

    val _isEmailVerified = mutableStateOf<Boolean?>(null)
    val isEmailVerified = _isEmailVerified
    private val _signUpState = MutableStateFlow(Common<FirebaseUser>())
    val signUpState = _signUpState.asStateFlow()

    private val _loginState = MutableStateFlow(Common<FirebaseUser>())
    val loginState = _loginState.asStateFlow()

    private val _googleSignInState = MutableStateFlow(Common<FirebaseUser>())
    val googleSignInState = _googleSignInState.asStateFlow()
    fun signUpUser(password: String, userModel: UserModel) {
        viewModelScope.launch(Dispatchers.IO) {
            _signUpState.value = Common(isLoading = true)
            Log.d("AuthViewModel", "Attempting signup for email: ${userModel.email}")

            when (val result = signUpUserUseCase.execute(password, userModel)) {
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

    fun loginUserWithEmailAndPassword(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {

            _loginState.value = Common(isLoading = true)


            val result = loginUserUseCase.signWithEmailAndPassword(email, password)

            when (result) {
                is ResultState.Error -> {
                    _loginState.value = Common(error = result.error.toString(), isLoading = false)
                }

                is ResultState.Success -> {
                    _loginState.value = Common(success = result.data, isLoading = false)
                }

                else -> {
                    Log.w("AuthViewModel", "Unexpected result state: $result")
                    _signUpState.value = Common(error = "Unexpected error occurred")
                }

            }
        }
    }






    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _googleSignInState.value = Common(isLoading = true)

            when (val result = signInWithGoogleUseCase(idToken)) {
                is ResultState.Error -> {
                    _googleSignInState.value = Common(
                        isLoading = false,
                        error = result.error
                    )
                }
                is ResultState.Success<*> -> {
                    _googleSignInState.value = Common(
                        isLoading = false,
                        success = result.data as? FirebaseUser
                    )
                }
                else -> {
                    _googleSignInState.value = Common(error = "Unexpected error occurred")
                }
            }
        }
    }



    fun checkEmailVerification(user: FirebaseUser?) {
        viewModelScope.launch {
            user?.reload()
            _isEmailVerified.value = user?.isEmailVerified
        }
    }
}

data class Common<T>(
    val isLoading: Boolean = false,
    val success: T? = null,
    val error: String = ""
)

