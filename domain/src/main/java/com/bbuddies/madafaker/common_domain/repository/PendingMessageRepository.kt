package com.bbuddies.madafaker.common_domain.repository

import com.bbuddies.madafaker.common_domain.model.PendingMessage

interface PendingMessageRepository {
    suspend fun savePendingMessage(pendingMessage: PendingMessage)
    suspend fun getAllPendingMessages(): List<PendingMessage>
    suspend fun getPendingMessageById(id: String): PendingMessage?
    suspend fun deletePendingMessage(id: String)
    suspend fun deleteAllPendingMessages()
    suspend fun updatePendingMessage(pendingMessage: PendingMessage)
    suspend fun hasPendingMessages(): Boolean
    suspend fun incrementRetryCount(id: String)
}