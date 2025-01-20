package com.bbuddies.madafaker.common_domain.model

data class User(
    val id: String, // is used as auth token
    val name: String,
    val coins: Int,
    val updatedAt: String,
    val createdAt: String
) // TODO
