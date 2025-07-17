package com.bbuddies.madafaker.common_domain.service

import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.FilterResult

/**
 * Service for filtering user-generated content based on the current mode
 */
interface ContentFilterService {

    /**
     * Performs client-side content filtering for the given text and mode.
     * Only applies filtering for SHINE mode - SHADOW mode returns allowed by default.
     *
     * @param text The text content to filter
     * @param mode The current mode (SHINE or SHADOW)
     * @return FilterResult indicating if content is allowed and any violations found
     */
    suspend fun filterContent(text: String, mode: Mode): FilterResult

    /**
     * Checks if the given text contains profanity using on-device detection.
     * This is a lightweight check used for instant feedback.
     *
     * @param text The text to check for profanity
     * @return FilterResult with profanity detection results
     */
    suspend fun checkProfanity(text: String): FilterResult

    /**
     * Updates the profanity word list (for future extensibility)
     *
     * @param words List of words to add to the filter
     */
    suspend fun updateProfanityList(words: List<String>)
}
