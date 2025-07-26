package com.bbuddies.madafaker.common_domain.repository

import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.Reply
import kotlinx.coroutines.flow.Flow

interface MessageRepository {

    fun observeIncomingMessages(): Flow<List<Message>>
    fun observeOutgoingMessages(): Flow<List<Message>>
    suspend fun refreshMessages()

    // Reply methods
    suspend fun createReply(body: String, parentId: String, isPublic: Boolean = true): Reply
    suspend fun getReplyById(id: String): Reply?
    suspend fun getRepliesByParentId(parentId: String): List<Reply>
    suspend fun getUserRepliesForMessage(parentId: String): List<Reply>

    suspend fun createMessage(body: String): Message

    // Updated methods for pending messages
//    suspend fun retryPendingMessages()
    suspend fun hasPendingMessages(): Boolean

    // Read state management
    suspend fun getMostRecentUnreadMessage(): Message?
    suspend fun markMessageAsRead(messageId: String)
    suspend fun markAllIncomingMessagesAsRead()
}