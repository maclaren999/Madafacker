package com.bbuddies.madafaker.presentation.base

import android.content.Context

/**
 * Class that represents result of an operation. Exposed to UI.
 *
 * */
sealed class MfResult<T> {
    data class Success<T>(val data: T) : MfResult<T>()

    /**
     * @param getErrorString - holds user-readable error string. Requires [Context] instance to be passed.
     * */
    data class Error<T>(val getErrorString: (context: Context) -> String, val data: T? = null) : MfResult<T>()
    class Loading<T> : MfResult<T>()
}