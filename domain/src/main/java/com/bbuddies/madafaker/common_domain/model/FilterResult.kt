package com.bbuddies.madafaker.common_domain.model

/**
 * Represents the result of content filtering operation
 */
data class FilterResult(
    val isAllowed: Boolean,
    val violationType: ViolationType? = null,
    val confidence: Float = 0.0f,
    val suggestion: String? = null,
    val detectedWords: List<String> = emptyList()
)

/**
 * Types of content violations that can be detected
 */
enum class ViolationType(val displayName: String) {
    PROFANITY("Profanity"),
    HATE_SPEECH("Hate Speech"),
    HARASSMENT("Harassment"),
    SEXUAL_CONTENT("Sexual Content"),
    VIOLENCE("Violence"),
    ILLEGAL_CONTENT("Illegal Content")
}

/**
 * Represents a moderation error from the server
 */
data class ModerationError(
    val code: String,
    val message: String,
    val violationType: ViolationType?,
    val suggestion: String?
)
