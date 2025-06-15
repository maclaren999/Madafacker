package com.bbuddies.madafaker.common_domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_messages")
data class PendingMessage(
    @PrimaryKey val id: String,
    val body: String,
    val mode: String,
    val createdAt: Long,
    val retryCount: Int = 0,
    val lastRetryAt: Long? = null
)