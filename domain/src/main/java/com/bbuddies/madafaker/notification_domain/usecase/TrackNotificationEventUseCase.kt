package com.bbuddies.madafaker.notification_domain.usecase

import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.notification_domain.repository.AnalyticsRepository
import javax.inject.Inject

class TrackNotificationEventUseCase @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) {

    suspend fun trackNotificationReceived(messageId: String, mode: Mode) {
        analyticsRepository.trackNotificationReceived(
            messageId = messageId,
            mode = mode,
            userTimezone = getCurrentTimezone()
        )
    }

    suspend fun trackNotificationOpened(messageId: String, mode: Mode, timeToOpen: Long) {
        analyticsRepository.trackNotificationOpened(
            messageId = messageId,
            mode = mode,
            timeToOpen = timeToOpen
        )
    }

    suspend fun trackNotificationDismissed(messageId: String, mode: Mode, timeInTray: Long) {
        analyticsRepository.trackNotificationDismissed(
            messageId = messageId,
            mode = mode,
            timeInTray = timeInTray
        )
    }

    suspend fun trackNotificationIgnored(messageId: String, mode: Mode, hoursIgnored: Int = 24) {
        analyticsRepository.trackNotificationIgnored(
            messageId = messageId,
            mode = mode,
            hoursIgnored = hoursIgnored
        )
    }

    suspend fun trackMessageRated(messageId: String, mode: Mode, rating: String, viaNotification: Boolean = true) {
        analyticsRepository.trackMessageRated(
            messageId = messageId,
            rating = rating,
            mode = mode,
            viaNotification = viaNotification
        )
    }

    suspend fun trackMessageReplied(messageId: String, mode: Mode, replyLength: Int, viaNotification: Boolean = true) {
        analyticsRepository.trackMessageReplied(
            messageId = messageId,
            mode = mode,
            viaNotification = viaNotification,
            replyLength = replyLength
        )
    }

    private fun getCurrentTimezone(): String {
        return java.util.TimeZone.getDefault().id
    }
}
