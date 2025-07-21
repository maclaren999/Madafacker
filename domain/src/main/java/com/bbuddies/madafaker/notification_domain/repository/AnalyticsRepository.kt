package com.bbuddies.madafaker.notification_domain.repository

import com.bbuddies.madafaker.common_domain.enums.Mode

interface AnalyticsRepository {

    /**
     * Track notification received event
     */
    fun trackNotificationReceived(
        messageId: String,
        mode: Mode,
        userTimezone: String
    )

    /**
     * Track notification opened event
     */
    fun trackNotificationOpened(
        messageId: String,
        mode: Mode,
        timeToOpen: Long,
        engagementRate: Float? = null
    )

    /**
     * Track notification dismissed event
     */
    fun trackNotificationDismissed(
        messageId: String,
        mode: Mode,
        timeInTray: Long
    )

    /**
     * Track notification ignored event
     */
    fun trackNotificationIgnored(
        messageId: String,
        mode: Mode,
        hoursIgnored: Int = 12
    )

    /**
     * Track message rated event
     */
    fun trackMessageRated(
        messageId: String,
        rating: String,
        mode: Mode,
        viaNotification: Boolean
    )

    /**
     * Track message replied event
     */
    fun trackMessageReplied(
        messageId: String,
        mode: Mode,
        viaNotification: Boolean,
        replyLength: Int
    )

    /**
     * Track custom event with parameters
     */
    fun trackCustomEvent(
        eventName: String,
        parameters: Map<String, Any>
    )
}
