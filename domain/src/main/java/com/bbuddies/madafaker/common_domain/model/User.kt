package com.bbuddies.madafaker.common_domain.model

data class User(
    val id: String,
    val name: String,
    val registrationToken: String? = null,
    val coins: Int = 0,
    val createdAt: String,
    val updatedAt: String
)
