package com.bbuddies.madafaker.common_domain.model

data class Message(  val id: String,
                     val body: String,
                     val mode: String,
                     val public: Boolean,
                     val createdAt: String,//TODO чи потрібні?
                     val updatedAt: String,//TODO
                     val authorId: String,//TODO
                     val parentId: String?)
