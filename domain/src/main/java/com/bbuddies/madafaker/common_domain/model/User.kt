package com.bbuddies.madafaker.common_domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val name: String,
    val registrationToken: String? = null,
    val coins: Int = 0,
    val createdAt: String,
    val updatedAt: String
)
