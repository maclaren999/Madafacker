package com.bbuddies.madafaker.notification_domain.usecase

import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.notification_domain.model.PlaceholderMessages
import com.bbuddies.madafaker.notification_domain.repository.NotificationRepository
import javax.inject.Inject

class GetPlaceholderMessageUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {

    suspend operator fun invoke(mode: Mode): String {
        return try {
            // Try to get messages from Remote Config first
            val remoteMessages = notificationRepository.getPlaceholderMessages(mode)
            if (remoteMessages.isNotEmpty()) {
                remoteMessages.random()
            } else {
                // Fallback to local messages
                PlaceholderMessages.getRandomMessage(mode)
            }
        } catch (e: Exception) {
            // Fallback to local messages if Remote Config fails
            PlaceholderMessages.getRandomMessage(mode)
        }
    }
}
