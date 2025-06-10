package com.bbuddies.madafaker.presentation.base

import android.content.Context

sealed class AuthResult<T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error<T>(val getErrorString: (context: Context) -> String) : AuthResult<T>()
    data class Unauthenticated<T>(val getErrorString: (context: Context) -> String) : AuthResult<T>()
    class Loading<T> : AuthResult<T>()
}