package com.bbuddies.madafaker.common_domain.repository

import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.Reply

interface MessageRepository {

    suspend fun getIncomingMassage(): List<Message>
    suspend fun getOutcomingMassage(): List<Message>
    suspend fun createReply(body: String? = null, isPublic: Boolean, parentId: String? = null)//TODO
    suspend fun getReplyById(id: String): Reply
    suspend fun updateReply(id: String, isPublic: Boolean)
    suspend fun createMessage(body: String): Message

}