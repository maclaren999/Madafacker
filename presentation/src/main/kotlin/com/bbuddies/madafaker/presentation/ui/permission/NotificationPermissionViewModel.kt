package com.bbuddies.madafaker.presentation.ui.permission

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.viewModelScope
import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import com.bbuddies.madafaker.presentation.base.BaseViewModel
import com.bbuddies.madafaker.presentation.utils.NotificationPermissionHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NotificationPermissionState {
    object Initial : NotificationPermissionState()
    object ShouldRequest : NotificationPermissionState()
    object Granted : NotificationPermissionState()
    object Denied : NotificationPermissionState()
    object AlreadyGranted : NotificationPermissionState()
}

@HiltViewModel
class NotificationPermissionViewModel @Inject constructor(
    private val application: Application,
    private val preferenceManager : PreferenceManager,
    private val notificationPermissionHelper: NotificationPermissionHelper
) : BaseViewModel() {

    private val _permissionState = MutableStateFlow<NotificationPermissionState>(NotificationPermissionState.Initial)
    val permissionState: StateFlow<NotificationPermissionState> = _permissionState

    private val _showSettingsPrompt = MutableStateFlow(false)
    val showSettingsPrompt: StateFlow<Boolean> = _showSettingsPrompt

    private val _shouldNavigateToMain = MutableStateFlow(false)
    val shouldNavigateToMain: StateFlow<Boolean> = _shouldNavigateToMain


    val currentMode = preferenceManager.currentMode

    init {
        checkInitialPermissionState()
    }

    private fun checkInitialPermissionState() {
        viewModelScope.launch {
            if (notificationPermissionHelper.isNotificationPermissionGranted()) {
                _permissionState.value = NotificationPermissionState.AlreadyGranted
                _shouldNavigateToMain.value = true
            } else {
                _permissionState.value = NotificationPermissionState.Initial
            }
        }
    }

    fun requestPermission() {
        _permissionState.value = NotificationPermissionState.ShouldRequest
    }

    fun onPermissionGranted() {
        viewModelScope.launch {
            _permissionState.value = NotificationPermissionState.Granted
            _shouldNavigateToMain.value = true
            // You might want to update the user's FCM token here
            // or perform any other post-permission tasks
        }
    }

    fun onPermissionDenied() {
        _permissionState.value = NotificationPermissionState.Denied
        _showSettingsPrompt.value = true
    }

    fun onSkip() {
        viewModelScope.launch {
            // Log analytics event for skipped notification permission
            // You might want to set a flag to not show this again for a while
            _shouldNavigateToMain.value = true
        }
    }

    fun openSettings() {
        try {
            val intent = Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", application.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            application.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to general settings if app-specific settings fail
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            application.startActivity(intent)
        }
    }

    fun dismissSettingsPrompt() {
        _showSettingsPrompt.value = false
    }

    fun onNavigationHandled() {
        _shouldNavigateToMain.value = false
    }
}