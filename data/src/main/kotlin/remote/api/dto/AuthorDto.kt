package remote.api.dto

/**
 * Network DTO for Author - nested object in Message/Reply responses (new API structure)
 */
data class AuthorDto(
    val id: String,
    val name: String,
    val createdAt: String
)
