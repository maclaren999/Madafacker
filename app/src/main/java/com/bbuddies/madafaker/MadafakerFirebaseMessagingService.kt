package com.bbuddies.madafaker

import android.util.Log
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.notification.NotificationManager
import com.bbuddies.madafaker.notification_domain.model.NotificationPayload
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MadafakerFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MadafakerFirebaseMsgService"
    }

    @Inject
    lateinit var notificationManager: NotificationManager

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "Message received with data: ${message.data}")

        // Handle data payload for silent notifications
        if (message.data.isNotEmpty()) {
            handleDataPayload(message.data)
        }

        // Legacy notification handling (fallback)w
        if (message.notification != null) {
            Log.d(TAG, "Received legacy notification: ${message.notification}")
        }
    }

    private fun handleDataPayload(data: Map<String, String>) {
        try {
            val messageId = data["messageId"] ?: return
            val modeString = data["mode"] ?: "shine"
            val timestamp = data["timestamp"] ?: ""
            val actualContent = data["actualContent"]

            val mode = Mode.fromApiValue(modeString)

            val payload = NotificationPayload(
                messageId = messageId,
                mode = mode,
                timestamp = timestamp,
                actualContent = actualContent
            )

            // Show notification using our custom manager
            coroutineScope.launch {
                notificationManager.showNotification(payload)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error handling notification payload", e)
        }
    }

    // Legacy notification method - kept for backward compatibility
    private fun sendNotification(messageBody: String?, title: String?) {
        Log.d(TAG, "Legacy notification method called - consider updating backend to use data payload")
        // This method is now deprecated in favor of data payload handling
    }


    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to your app server.
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
    }
}