package com.bbuddies.madafaker.common_domain.usecase

import com.bbuddies.madafaker.common_domain.enums.MessageRating
import com.bbuddies.madafaker.common_domain.repository.MessageRepository
import javax.inject.Inject

class RateMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {

    suspend operator fun invoke(
        messageId: String,
        rating: MessageRating
    ): Result<Unit> {
        return try {
            // Validate input
            if (messageId.isBlank()) {
                return Result.failure(IllegalArgumentException("Message ID cannot be empty"))
            }

            messageRepository.rateMessage(messageId, rating)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
