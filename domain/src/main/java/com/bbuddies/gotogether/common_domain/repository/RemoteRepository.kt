package com.bbuddies.gotogether.common_domain.repository

import com.bbuddies.gotogether.common_domain.model.Message
import com.bbuddies.gotogether.common_domain.model.Reply
import com.bbuddies.gotogether.common_domain.model.User

interface RemoteRepository {
    suspend fun getCurrentUser(): User
    suspend fun getIncomingMassage(): List<Message>
    suspend fun getOutcomingMassage(): List<Message>
    suspend fun getReplyById(id: String): Reply


    suspend fun updateCurrentUser(name: String): User
    suspend fun updateReply(id: String, isPublic: Boolean)

    suspend fun createUser(name: String): User
    suspend fun createMessage(body: String, mode: String): Message
    suspend fun createReply(body: String? = null, isPublic: Boolean, parentId: String? = null)//TODO


}