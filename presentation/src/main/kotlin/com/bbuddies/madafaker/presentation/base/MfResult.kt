package com.bbuddies.madafaker.presentation.base

sealed class MfResult<T> {
    data class Success<T>(val data: T) : MfResult<T>()
    data class Error<T>(val message: String, val data: T? = null) : MfResult<T>()
    class Loading<T> : MfResult<T>()
}