package com.example.smartwaste_user.presentation.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartwaste_user.common.ResultState
import com.example.smartwaste_user.data.models.UserModel
import com.example.smartwaste_user.domain.usecase.userusecase.GetUserDetailsUseCase
import com.example.smartwaste_user.domain.usecase.userusecase.UpdateUserDetailsUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set


@HiltViewModel
class UserViewModel @Inject constructor(
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val updateUserDetailsUseCase: UpdateUserDetailsUseCase
) : ViewModel() {

    private val _userState = MutableStateFlow(CommonUserState<UserModel>())
    val userState = _userState.asStateFlow()

    private val _updateState = MutableStateFlow(CommonUserState<String>())
    val updateState = _updateState.asStateFlow()


    private val _qrCodeBitmap = MutableStateFlow<Bitmap?>(null)
    val qrCodeBitmap = _qrCodeBitmap.asStateFlow()

    init {
        getUserData()
        generateUserQRCode()
    }

    fun getUserData() {
        viewModelScope.launch(Dispatchers.IO) {
            getUserDetailsUseCase.getUserDetailsUseCase().collect {

                when (it) {
                    is ResultState.Loading -> {
                        _userState.value = CommonUserState(isLoading = true)
                    }

                    is ResultState.Success -> {
                        _userState.value = CommonUserState(succcess = it.data, isLoading = false)
                    }


                    is ResultState.Error -> {
                        _userState.value = CommonUserState(error = it.error, isLoading = false)
                    }


                }
            }

        }
    }


    fun updateUserData(userModel: UserModel) {
        viewModelScope.launch(Dispatchers.IO) {

            updateUserDetailsUseCase.updateUserDetailsUseCase(userModel).let {
                when (it) {
                    is ResultState.Loading -> {
                        _updateState.value = CommonUserState(isLoading = true)
                    }

                    is ResultState.Success -> {
                        _updateState.value = CommonUserState(succcess = it.data, isLoading = false)
                    }


                    is ResultState.Error -> {
                        _updateState.value = CommonUserState(error = it.error, isLoading = false)
                    }

                }


            }
        }

    }

    private fun generateUserQRCode() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        _qrCodeBitmap.value = generateQRCode(uid)
    }

    private fun generateQRCode(content: String, width: Int = 512, height: Int = 512): Bitmap {
        val bitMatrix: BitMatrix =
            MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height)
        val bitmap = createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap[x, y] =
                    if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
            }
        }
        return bitmap
    }


}

data class CommonUserState<T>(
    val isLoading: Boolean = false,
    val succcess: T? = null,
    val error: String = ""

)