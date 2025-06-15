package com.bbuddies.madafaker.common_domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "replies")
data class Reply(
    @PrimaryKey val id: String,
    val body: String,
    val mode: String,
    val isPublic: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val authorId: String,
    val parentId: String?,
    val replies: List<Reply> = emptyList()
)