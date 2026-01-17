package remote.api.dto

/**
 * Network DTO for RatingStats - statistics about message ratings.
 * Matches 2026 API structure exactly.
 */
data class RatingStatsDto(
    val likes: Int = 0,
    val dislikes: Int = 0,
    val superLikes: Int = 0
)
