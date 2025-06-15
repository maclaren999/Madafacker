package com.bbuddies.madafaker.common_domain.repository

import com.bbuddies.madafaker.common_domain.model.UnsentDraft
import kotlinx.coroutines.flow.Flow

interface DraftRepository {

    /**
     * Save draft message
     */
    suspend fun saveDraft(draft: UnsentDraft)

    /**
     * Get current draft as Flow
     */
    fun getCurrentDraft(): Flow<UnsentDraft?>

    /**
     * Clear the current draft
     */
    suspend fun clearDraft()

    /**
     * Get draft synchronously (for initial load)
     */
    suspend fun getDraftOnce(): UnsentDraft?
}