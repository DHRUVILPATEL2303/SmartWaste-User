package com.example.smartwaste_user.common

sealed class ResultState<out T> {


    data class Success<out T>(val data : T): ResultState<T>()
    data class Error(val error : String) : ResultState<Nothing>()
    object Loading : ResultState<Nothing>()
}