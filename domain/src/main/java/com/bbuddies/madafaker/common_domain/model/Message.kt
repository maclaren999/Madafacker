package com.bbuddies.madafaker.common_domain.model

data class Message(
    val id: String,
    val body: String,
    val mode: String,
    val isPublic: Boolean,
    val createdAt: String,//TODO чи потрібні?
    val authorId: String,//TODO
    val up: Int? = null,
    val down: Int? = null,
    val hearts: Int? = null
)
