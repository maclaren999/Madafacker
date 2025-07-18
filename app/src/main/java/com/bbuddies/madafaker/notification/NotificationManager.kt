package com.bbuddies.madafaker.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.bbuddies.madafaker.R
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.notification_domain.model.NotificationPayload
import com.bbuddies.madafaker.notification_domain.usecase.GetPlaceholderMessageUseCase
import com.bbuddies.madafaker.notification_domain.usecase.TrackNotificationEventUseCase
import com.bbuddies.madafaker.presentation.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getPlaceholderMessageUseCase: GetPlaceholderMessageUseCase,
    private val trackNotificationEventUseCase: TrackNotificationEventUseCase
) {

    companion object {
        private const val CHANNEL_ID = "madafaker_messages"
        private const val NOTIFICATION_ID = 1001

        // Intent extras for deep linking
        const val EXTRA_MESSAGE_ID = "message_id"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_MODE = "mode"
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Message Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for new messages"
            setSound(null, null) // Silent notifications
            enableVibration(false)
            setShowBadge(true)
        }

        notificationManager.createNotificationChannel(channel)
    }

    suspend fun showNotification(payload: NotificationPayload) {
        val placeholderMessage = getPlaceholderMessageUseCase(payload.mode)
        val notificationId = generateNotificationId()

        // Track notification received event
        trackNotificationReceived(payload.messageId, payload.mode)

        val intent = createDeepLinkIntent(payload, notificationId)
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(getNotificationIcon(payload.mode))
            .setContentTitle(getNotificationTitle(payload.mode))
            .setContentText(placeholderMessage)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(placeholderMessage))
            .setColor(getNotificationColor(payload.mode))
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createDeepLinkIntent(payload: NotificationPayload, notificationId: String): Intent {
        return Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_MESSAGE_ID, payload.messageId)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(EXTRA_MODE, payload.mode.name)
        }
    }

    private fun getNotificationIcon(mode: Mode): Int {
        return when (mode) {
            Mode.SHINE -> R.drawable.ic_launcher_foreground // Replace with sun icon
            Mode.SHADOW -> R.drawable.ic_launcher_foreground // Replace with moon icon
        }
    }

    private fun getNotificationTitle(mode: Mode): String {
        return when (mode) {
            Mode.SHINE -> "Shine Message"
            Mode.SHADOW -> "Shadow Message"
        }
    }

    private fun getNotificationColor(mode: Mode): Int {
        return when (mode) {
            Mode.SHINE -> 0xFFFFD700.toInt() // Gold
            Mode.SHADOW -> 0xFF6A5ACD.toInt() // Purple
        }
    }

    private fun generateNotificationId(): String {
        return "notif_${System.currentTimeMillis()}"
    }

    private fun trackNotificationReceived(messageId: String, mode: Mode) {
        coroutineScope.launch {
            try {
                trackNotificationEventUseCase.trackNotificationReceived(messageId, mode)
            } catch (e: Exception) {
                // Silently fail - event tracking shouldn't crash the app
            }
        }
    }

    private fun trackNotificationOpened(messageId: String, mode: Mode, timeToOpen: Long) {
        coroutineScope.launch {
            try {
                trackNotificationEventUseCase.trackNotificationOpened(messageId, mode, timeToOpen)
            } catch (e: Exception) {
                // Silently fail - event tracking shouldn't crash the app
            }
        }
    }

    private fun trackNotificationDismissed(messageId: String, mode: Mode, timeInTray: Long) {
        coroutineScope.launch {
            try {
                trackNotificationEventUseCase.trackNotificationDismissed(messageId, mode, timeInTray)
            } catch (e: Exception) {
                // Silently fail - event tracking shouldn't crash the app
            }
        }
    }

    fun handleNotificationOpened(messageId: String, notificationId: String, mode: Mode) {
        val timeToOpen = calculateTimeToOpen(notificationId)
        trackNotificationOpened(messageId, mode, timeToOpen)
    }

    fun handleNotificationDismissed(messageId: String, notificationId: String, mode: Mode) {
        val timeInTray = calculateTimeInTray(notificationId)
        trackNotificationDismissed(messageId, mode, timeInTray)
    }

    private fun calculateTimeToOpen(notificationId: String): Long {
        // Extract timestamp from notification ID and calculate difference
        return try {
            val timestamp = notificationId.substringAfter("notif_").toLong()
            (System.currentTimeMillis() - timestamp) / 1000
        } catch (e: Exception) {
            0L
        }
    }

    private fun calculateTimeInTray(notificationId: String): Long {
        // Similar to calculateTimeToOpen
        return calculateTimeToOpen(notificationId)
    }
}
