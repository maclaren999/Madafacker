package com.bbuddies.madafaker.presentation.base

import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.MutableStateFlow

abstract class BaseViewModel : ViewModel() {

    val warningsFlow = MutableStateFlow<String?>(null)

    protected suspend fun warnUser(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
        warningsFlow.emit(message)
    }

}
