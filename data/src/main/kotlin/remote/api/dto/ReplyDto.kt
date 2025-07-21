package remote.api.dto

import com.bbuddies.madafaker.common_domain.model.Reply

/**
 * Network DTO for Reply - matches API response structure
 */
data class ReplyDto(
    val id: String,
    val body: String,
    val mode: String,
    val isPublic: Boolean,
    val wasSent: Boolean? = null,
    val createdAt: String,
    val updatedAt: String,
    val authorId: String,
    val parentId: String?
)

// Extension functions for mapping
fun ReplyDto.toDomainModel(): Reply {
    return Reply(
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
