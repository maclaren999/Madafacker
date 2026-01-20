package com.bbuddies.madafaker.presentation.ui.permission

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.viewModelScope
import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import com.bbuddies.madafaker.notification_domain.repository.AnalyticsRepository
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
    private val notificationPermissionHelper: NotificationPermissionHelper,
    private val analyticsRepository: AnalyticsRepository
) : BaseViewModel() {

    private val _permissionState = MutableStateFlow<NotificationPermissionState>(NotificationPermissionState.Initial)
    val permissionState: StateFlow<NotificationPermissionState> = _permissionState

    private val _showSettingsPrompt = MutableStateFlow(false)
    val showSettingsPrompt: StateFlow<Boolean> = _showSettingsPrompt

    private val _shouldNavigateToMain = MutableStateFlow(false)
    val shouldNavigateToMain: StateFlow<Boolean> = _shouldNavigateToMain


    val currentMode = preferenceManager.currentMode
    private var hasRequestedPermission = false

    init {
        refreshPermissionState()
    }

    fun refreshPermissionState() {
        viewModelScope.launch {
            if (notificationPermissionHelper.isNotificationPermissionGranted()) {
                _permissionState.value = NotificationPermissionState.AlreadyGranted
                _shouldNavigateToMain.value = true
                _showSettingsPrompt.value = false
            } else {
                if (_permissionState.value == NotificationPermissionState.AlreadyGranted ||
                    _permissionState.value == NotificationPermissionState.Granted
                ) {
                    _permissionState.value = NotificationPermissionState.Initial
                }
            }
        }
    }

    fun requestPermission(shouldShowRationale: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!shouldShowRationale && hasRequestedPermission) {
                openSettingsFromEnableButton()
                _showSettingsPrompt.value = true
            } else {
                hasRequestedPermission = true
                _permissionState.value = NotificationPermissionState.ShouldRequest
            }
        } else {
            openSettingsFromEnableButton()
        }
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

    fun openSettingsFromEnableButton() {
        trackOpenSettings("enable_button")
        openSettings()
    }

    fun openSettingsFromSnackbar() {
        trackOpenSettings("snackbar")
        openSettings()
    }

    private fun openSettings() {
        try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, application.packageName)
                }
            } else {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", application.packageName, null)
                }
            }.apply {
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

    private fun trackOpenSettings(source: String) {
        val parameters = mapOf(
            "source" to source,
            "sdk_int" to Build.VERSION.SDK_INT
        )
        analyticsRepository.trackCustomEvent("notification_settings_opened", parameters)
    }

    fun dismissSettingsPrompt() {
        _showSettingsPrompt.value = false
    }

    fun onNavigationHandled() {
        _shouldNavigateToMain.value = false
    }
}
