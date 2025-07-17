package com.bbuddies.madafaker.common_domain.exception

import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.ViolationType

/**
 * Exception thrown when content moderation fails
 */
sealed class ModerationException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    /**
     * Client-side moderation failure (e.g., profanity filter)
     */
    class ClientSideViolation(
        val violationType: ViolationType,
        val detectedWords: List<String> = emptyList(),
        val mode: Mode,
        message: String = "Content violates community guidelines"
    ) : ModerationException(message)

    /**
     * Server-side moderation failure (e.g., OpenAI Moderation API)
     */
    class ServerSideViolation(
        val violationType: ViolationType?,
        val mode: Mode,
        val serverMessage: String,
        val suggestion: String? = null,
        cause: Throwable? = null
    ) : ModerationException(serverMessage, cause)

    /**
     * Rate limiting from moderation service
     */
    class RateLimited(
        val retryAfterSeconds: Int? = null,
        message: String = "Too many requests. Please try again later."
    ) : ModerationException(message)

    /**
     * General moderation service error
     */
    class ServiceError(
        message: String = "Moderation service temporarily unavailable",
        cause: Throwable? = null
    ) : ModerationException(message, cause)
}
