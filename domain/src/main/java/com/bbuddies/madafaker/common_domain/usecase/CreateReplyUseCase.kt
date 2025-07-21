package com.bbuddies.madafaker.common_domain.usecase

import com.bbuddies.madafaker.common_domain.model.Reply
import com.bbuddies.madafaker.common_domain.repository.MessageRepository
import javax.inject.Inject

class CreateReplyUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {

    suspend operator fun invoke(
        body: String,
        parentId: String,
        isPublic: Boolean = true
    ): Result<Reply> {
        return try {
            // Validate input
            if (body.isBlank()) {
                return Result.failure(IllegalArgumentException("Reply body cannot be empty"))
            }

            if (body.length > 500) {
                return Result.failure(IllegalArgumentException("Reply body cannot exceed 500 characters"))
            }

            val reply = messageRepository.createReply(
                body = body.trim(),
                parentId = parentId,
                isPublic = isPublic
            )

            Result.success(reply)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
