package com.bbuddies.madafaker.common_domain.model

data class User(
    val id: String, // is used as auth token
    val name: String,
    val updatedAt: String, // TODO  чи потрібні?
    val createdAt: String
) // TODO
