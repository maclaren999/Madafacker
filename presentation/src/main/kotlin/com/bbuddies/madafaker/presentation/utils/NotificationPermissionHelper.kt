package com.bbuddies.madafaker.presentation.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class NotificationPermissionHelper(
    private val context: Context
) {

    /**
     * Checks if notification permission is granted
     * For Android 13+ (API 33+), checks POST_NOTIFICATIONS permission
     * For older versions, checks if notifications are enabled via NotificationManagerCompat
     */
    fun isNotificationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ - check runtime permission
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Older versions - check if notifications are enabled
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    /**
     * Checks if we should show the permission request screen
     * Returns true if permission is not granted and we haven't been permanently denied
     */
    fun shouldShowPermissionScreen(): Boolean {
        return !isNotificationPermissionGranted()
    }
}