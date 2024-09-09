package com.bbuddies.madafaker.presentation.base

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class BaseViewModel : ViewModel() {

    protected val _warningsFlow: MutableStateFlow<((context: Context) -> String?)?> = MutableStateFlow(null)
    val warningsFlow: StateFlow<((context: Context) -> String?)?> = _warningsFlow // TODO: Investigate NPE

}