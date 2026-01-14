package remote.api.dto

import com.bbuddies.madafaker.common_domain.model.Reply

/**
 * Network DTO for Reply - matches API response structure
 * 
 * Note: API was updated in 2026. New API structure:
 * - 'authorId' replaced with 'author' object containing {id, name, createdAt}
 * - 'updatedAt' removed from replies (now nullable)
 * - 'public', 'wasSent', 'parentId' removed from replies (now nullable)
 */
data class ReplyDto(
    val id: String,
    val body: String,
    val mode: String,
    // Old API fields (now nullable for backward compatibility)
    val isPublic: Boolean? = null,
    val wasSent: Boolean? = null,
    val updatedAt: String? = null,
    val authorId: String? = null,
    val parentId: String? = null,
    // Required fields
    val createdAt: String,
    // New API fields
    val author: AuthorDto? = null
)

// Extension functions for mapping
fun ReplyDto.toDomainModel(): Reply {
    // Derive authorId from author object if authorId is not directly available
    val resolvedAuthorId = authorId ?: author?.id ?: ""
    
    return Reply(
        id = id,
        body = body,
        mode = mode,
        isPublic = isPublic ?: true,
        createdAt = createdAt,
        // Use createdAt as fallback when updatedAt is not provided
        updatedAt = updatedAt ?: createdAt,
        authorId = resolvedAuthorId,
        parentId = parentId
    )
}

fun Reply.toNetworkDto(): ReplyDto {
    return ReplyDto(
        id = id,
        body = body,
        mode = mode,
        isPublic = isPublic,
        createdAt = createdAt,
        updatedAt = updatedAt,
        authorId = authorId,
        parentId = parentId
    )
}
