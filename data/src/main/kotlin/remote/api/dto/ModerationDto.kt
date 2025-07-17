package remote.api.dto

import com.squareup.moshi.Json

/**
 * DTO for moderation error responses from the server
 */
data class ModerationErrorDto(
    @Json(name = "error") val error: String,
    @Json(name = "code") val code: String,
    @Json(name = "violationType") val violationType: String? = null,
    @Json(name = "suggestion") val suggestion: String? = null,
    @Json(name = "details") val details: String? = null
)

/**
 * DTO for moderation request (future use)
 */
data class ModerationRequestDto(
    @Json(name = "content") val content: String,
    @Json(name = "mode") val mode: String,
    @Json(name = "context") val context: String? = null
)

/**
 * DTO for moderation response (future use)
 */
data class ModerationResponseDto(
    @Json(name = "allowed") val allowed: Boolean,
    @Json(name = "violationType") val violationType: String? = null,
    @Json(name = "confidence") val confidence: Float = 0.0f,
    @Json(name = "reason") val reason: String? = null,
    @Json(name = "suggestion") val suggestion: String? = null
)
