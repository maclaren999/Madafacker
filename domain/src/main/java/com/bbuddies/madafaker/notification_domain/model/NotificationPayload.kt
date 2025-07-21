package com.bbuddies.madafaker.notification_domain.model

import com.bbuddies.madafaker.common_domain.enums.Mode

/**
 * Data structure for FCM notification payload
 */
data class NotificationPayload(
    val messageId: String,
    val mode: Mode,
    val timestamp: String,
    val actualContent: String? = null // Hidden until app is opened
)
