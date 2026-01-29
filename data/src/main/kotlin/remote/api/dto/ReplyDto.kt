package remote.api.dto

import com.bbuddies.madafaker.common_domain.model.Reply

/**
 * Network DTO for Reply - matches 2026 API structure exactly.
 * Replies are nested within messages and contain author object.
 */
data class ReplyDto(
    val id: String,
    val body: String,
    val author: AuthorDto,
    val mode: String,
    val createdAt: String,
    val parentId: String? = null,
    val public: Boolean? = null
)

/**
 * Maps ReplyDto to domain model.
 * @param parentMessageId The ID of the parent message (for client-side tracking)
 */
fun ReplyDto.toDomainModel(parentMessageId: String? = null): Reply {
    val resolvedParentId = parentMessageId ?: parentId
    return Reply(
        id = id,
        body = body,
        mode = mode,
        createdAt = createdAt,
        authorId = author.id,
        authorName = author.name,
        parentMessageId = resolvedParentId
    )
}
