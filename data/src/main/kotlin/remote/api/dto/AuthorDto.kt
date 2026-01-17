package remote.api.dto

/**
 * Network DTO for Author - nested object in Message/Reply responses.
 * Matches 2026 API structure exactly.
 */
data class AuthorDto(
    val id: String,
    val name: String,
    val createdAt: String
)
