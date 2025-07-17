package com.bbuddies.madafaker.common_domain.exception

import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.ViolationType
import kotlin.test.DefaultAsserter.assertEquals
import kotlin.test.DefaultAsserter.assertNull
import kotlin.test.DefaultAsserter.assertTrue
import kotlin.test.Test

class ModerationExceptionTest {

    @Test
    fun `ClientSideViolation should contain correct information`() {
        // Given
        val violationType = ViolationType.PROFANITY
        val detectedWords = listOf("damn", "shit")
        val mode = Mode.SHINE
        val message = "Content violates community guidelines"

        // When
        val exception = ModerationException.ClientSideViolation(
            violationType = violationType,
            detectedWords = detectedWords,
            mode = mode,
            message = message
        )

        // Then
        assertEquals("Should have correct violation type", violationType, exception.violationType)
        assertEquals("Should have correct detected words", detectedWords, exception.detectedWords)
        assertEquals("Should have correct mode", mode, exception.mode)
        assertEquals("Should have correct message", message, exception.message)
    }

    @Test
    fun `ServerSideViolation should contain correct information`() {
        // Given
        val violationType = ViolationType.HATE_SPEECH
        val mode = Mode.SHINE
        val serverMessage = "Content rejected by server moderation"
        val suggestion = "Please rephrase your message"

        // When
        val exception = ModerationException.ServerSideViolation(
            violationType = violationType,
            mode = mode,
            serverMessage = serverMessage,
            suggestion = suggestion
        )

        // Then
        assertEquals("Should have correct violation type", violationType, exception.violationType)
        assertEquals("Should have correct mode", mode, exception.mode)
        assertEquals("Should have correct server message", serverMessage, exception.serverMessage)
        assertEquals("Should have correct suggestion", suggestion, exception.suggestion)
        assertEquals("Should use server message as exception message", serverMessage, exception.message)
    }

    @Test
    fun `RateLimited should contain correct information`() {
        // Given
        val retryAfterSeconds = 30
        val message = "Too many requests. Please try again later."

        // When
        val exception = ModerationException.RateLimited(
            retryAfterSeconds = retryAfterSeconds,
            message = message
        )

        // Then
        assertEquals("Should have correct retry after seconds", retryAfterSeconds, exception.retryAfterSeconds)
        assertEquals("Should have correct message", message, exception.message)
    }

    @Test
    fun `ServiceError should contain correct information`() {
        // Given
        val message = "Moderation service temporarily unavailable"
        val cause = RuntimeException("Network error")

        // When
        val exception = ModerationException.ServiceError(
            message = message,
            cause = cause
        )

        // Then
        assertEquals("Should have correct message", message, exception.message)
        assertEquals("Should have correct cause", cause, exception.cause)
    }

    @Test
    fun `ClientSideViolation should use default message when not provided`() {
        // Given
        val violationType = ViolationType.PROFANITY
        val mode = Mode.SHINE

        // When
        val exception = ModerationException.ClientSideViolation(
            violationType = violationType,
            detectedWords = emptyList(),
            mode = mode
            // message parameter not provided, should use default
        )

        // Then
        assertEquals("Should use default message", "Content violates community guidelines", exception.message)
    }

    @Test
    fun `RateLimited should use default message when not provided`() {
        // When
        val exception = ModerationException.RateLimited()

        // Then
        assertEquals("Should use default message", "Too many requests. Please try again later.", exception.message)
        assertNull("Should have no retry after seconds", exception.retryAfterSeconds)
    }

    @Test
    fun `ServiceError should use default message when not provided`() {
        // When
        val exception = ModerationException.ServiceError()

        // Then
        assertEquals("Should use default message", "Moderation service temporarily unavailable", exception.message)
        assertNull("Should have no cause", exception.cause)
    }

    @Test
    fun `all exceptions should extend ModerationException`() {
        // Given
        val clientException = ModerationException.ClientSideViolation(
            ViolationType.PROFANITY, emptyList(), Mode.SHINE
        )
        val serverException = ModerationException.ServerSideViolation(
            ViolationType.HATE_SPEECH, Mode.SHADOW, "Server error"
        )
        val rateLimitException = ModerationException.RateLimited()
        val serviceException = ModerationException.ServiceError()

        // Then
        assertTrue(
            "ClientSideViolation should extend ModerationException",
            clientException is ModerationException
        )
        assertTrue(
            "ServerSideViolation should extend ModerationException",
            serverException is ModerationException
        )
        assertTrue(
            "RateLimited should extend ModerationException",
            rateLimitException is ModerationException
        )
        assertTrue(
            "ServiceError should extend ModerationException",
            serviceException is ModerationException
        )
    }
}
