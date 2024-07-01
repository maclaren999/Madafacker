package com.bbuddies.gotogether.common_domain.repository

import com.bbuddies.gotogether.common_domain.model.Message
import com.bbuddies.gotogether.common_domain.model.Reply
import com.bbuddies.gotogether.common_domain.model.User

interface RemoteRepository {
    suspend fun getCurrentUser(): User
    suspend fun getIncomingMassage(): Message
    suspend fun getOutcomingMassage():Message
    suspend fun getReplyById(id: String):Reply



    suspend fun updateCurrentUser(name: String)
    suspend fun updateReply(id: String, isPublic: Boolean)

    suspend fun createUser(name: String)
    suspend fun createMessage(body: String, mode: String)
    suspend fun createReply(body: String? = null, isPublic: Boolean, parentId: String? = null)//TODO





}