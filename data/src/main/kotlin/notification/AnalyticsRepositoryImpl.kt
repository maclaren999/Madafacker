package com.bbuddies.madafaker.data.notification

import android.os.Bundle
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.notification_domain.repository.AnalyticsRepository
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsRepositoryImpl @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) : AnalyticsRepository {

    override fun trackNotificationReceived(
        messageId: String,
        mode: Mode,
        userTimezone: String
    ) {
        val bundle = Bundle().apply {
            putString("message_id", messageId)
            putString("mode", mode.displayName.lowercase())
            putString("user_timezone", userTimezone)
        }
        firebaseAnalytics.logEvent("notification_received", bundle)
    }

    override fun trackNotificationOpened(
        messageId: String,
        mode: Mode,
        timeToOpen: Long,
        engagementRate: Float?
    ) {
        val bundle = Bundle().apply {
            putString("message_id", messageId)
            putString("mode", mode.displayName.lowercase())
            putLong("time_to_open", timeToOpen)
            engagementRate?.let { putDouble("engagement_rate", it.toDouble()) }
        }
        firebaseAnalytics.logEvent("notification_opened", bundle)
    }

    override fun trackNotificationDismissed(
        messageId: String,
        mode: Mode,
        timeInTray: Long
    ) {
        val bundle = Bundle().apply {
            putString("message_id", messageId)
            putString("mode", mode.displayName.lowercase())
            putLong("time_in_tray", timeInTray)
        }
        firebaseAnalytics.logEvent("notification_dismissed", bundle)
    }

    override fun trackNotificationIgnored(
        messageId: String,
        mode: Mode,
        hoursIgnored: Int
    ) {
        val bundle = Bundle().apply {
            putString("message_id", messageId)
            putString("mode", mode.displayName.lowercase())
            putLong("hours_ignored", hoursIgnored.toLong())
        }
        firebaseAnalytics.logEvent("notification_ignored", bundle)
    }

    override fun trackMessageRated(
        messageId: String,
        rating: String,
        mode: Mode,
        viaNotification: Boolean
    ) {
        val bundle = Bundle().apply {
            putString("message_id", messageId)
            putString("rating", rating)
            putString("mode", mode.displayName.lowercase())
            putLong("via_notification", if (viaNotification) 1L else 0L)
        }
        firebaseAnalytics.logEvent("message_rated", bundle)
    }

    override fun trackMessageReplied(
        messageId: String,
        mode: Mode,
        viaNotification: Boolean,
        replyLength: Int
    ) {
        val bundle = Bundle().apply {
            putString("message_id", messageId)
            putString("mode", mode.displayName.lowercase())
            putLong("via_notification", if (viaNotification) 1L else 0L)
            putLong("reply_length", replyLength.toLong())
        }
        firebaseAnalytics.logEvent("message_replied", bundle)
    }

    override fun trackCustomEvent(
        eventName: String,
        parameters: Map<String, Any>
    ) {
        val bundle = Bundle().apply {
            parameters.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Int -> putLong(key, value.toLong())
                    is Float -> putDouble(key, value.toDouble())
                    is Boolean -> putLong(key, if (value) 1L else 0L)
                    else -> putString(key, value.toString())
                }
            }
        }
        firebaseAnalytics.logEvent(eventName, bundle)
    }
}
