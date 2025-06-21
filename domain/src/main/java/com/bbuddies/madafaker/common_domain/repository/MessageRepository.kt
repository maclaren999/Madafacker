package com.bbuddies.madafaker.common_domain.repository

import com.bbuddies.madafaker.common_domain.model.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {

    fun observeIncomingMessages(): Flow<List<Message>>?
    fun observeOutgoingMessages(): Flow<List<Message>>?
    suspend fun refreshMessages()

    //TODO: Reply logic
//    suspend fun createReply(body: String? = null, isPublic: Boolean, parentId: String? = null)
//    suspend fun getReplyById(id: String): Reply

    suspend fun createMessage(body: String): Message

    // Updated methods for pending messages
    suspend fun retryPendingMessages()
    suspend fun hasPendingMessages(): Boolean
}