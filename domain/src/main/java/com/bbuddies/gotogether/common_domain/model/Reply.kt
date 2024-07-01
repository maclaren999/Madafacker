package com.bbuddies.gotogether.common_domain.model

data class Reply( val id: String,
                  val body: String,
                  val mode: String,
                  val public: Boolean,
                  val createdAt: String,//TODO
                  val updatedAt: String,//TODO
                  val authorId: String,//TODO
                  val parentId: String?,
                  val replies: List<Reply> = emptyList())
