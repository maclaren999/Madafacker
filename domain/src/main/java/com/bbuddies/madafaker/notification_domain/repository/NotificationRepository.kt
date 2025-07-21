package com.bbuddies.madafaker.notification_domain.repository

import com.bbuddies.madafaker.common_domain.enums.Mode

interface NotificationRepository {

    /**
     * Get placeholder messages for the specified mode from Remote Config
     */
    suspend fun getPlaceholderMessages(mode: Mode): List<String>

    /**
     * Get notification frequency configuration from Remote Config
     */
    suspend fun getNotificationConfig(): Map<String, Any>
}
