package com.bbuddies.madafaker.common_domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Reply domain model - matches 2026 API structure.
 * Replies are nested within messages and contain author info.
 */
@Serializable
@Entity(tableName = "replies")
data class Reply(
    @PrimaryKey val id: String,
    val body: String,
    val mode: String,
    val createdAt: String,
    // Author info (denormalized from nested author object)
    val authorId: String,
    val authorName: String,
    // Parent message reference (client-side tracking, not from API)
    val parentMessageId: String? = null
)