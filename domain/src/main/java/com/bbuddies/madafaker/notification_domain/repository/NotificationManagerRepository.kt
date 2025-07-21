package com.bbuddies.madafaker.notification_domain.repository

/**
 * Repository interface for notification management operations
 * This allows the presentation layer to interact with notifications
 * without directly depending on the app layer's NotificationManager
 */
interface NotificationManagerRepository {

    /**
     * Dismiss all notifications from the system tray
     */
    fun dismissAllNotifications()

    /**
     * Handle notification opened event
     */
    fun handleNotificationOpened(messageId: String, notificationId: String, mode: String)

    /**
     * Handle notification dismissed event
     */
    fun handleNotificationDismissed(messageId: String, notificationId: String, mode: String)
}
