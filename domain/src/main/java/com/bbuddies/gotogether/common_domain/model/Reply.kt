package com.bbuddies.gotogether.common_domain.model

data class Reply( val id: String,
                  val body: String,
                  val mode: String,
                  val public: Boolean,
                  val createdAt: String,//TODO чи потрібні?
                  val updatedAt: String,//TODO чи потрібні?
                  val authorId: String,//TODO чи потрібні?
                  val parentId: String?,
                  val replies: List<Reply> = emptyList())
