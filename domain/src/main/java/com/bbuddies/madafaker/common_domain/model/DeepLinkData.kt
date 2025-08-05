package com.bbuddies.madafaker.common_domain.model

import com.bbuddies.madafaker.common_domain.enums.Mode
import kotlinx.serialization.Serializable

/**
 * Deep link data for navigation between screens
 * 
 * Used when the app is opened via notification or external intent
 * with specific message/notification targeting.
 */
@Serializable
data class DeepLinkData(
    val messageId: String,
    val notificationId: String,
    val mode: Mode
) {
    /**
     * Validates that all required fields are present and not empty
     */
    fun isValid(): Boolean {
        return messageId.isNotBlank() && 
               notificationId.isNotBlank()
    }
    
    companion object {
        /**
         * Creates DeepLinkData from intent extras
         */
        fun fromIntentExtras(
            messageId: String?,
            notificationId: String?,
            modeString: String?
        ): DeepLinkData? {
            if (messageId.isNullOrBlank() || notificationId.isNullOrBlank()) {
                return null
            }
            
            val mode = try {
                modeString?.let { Mode.valueOf(it) } ?: Mode.SHINE
            } catch (e: IllegalArgumentException) {
                Mode.SHINE
            }
            
            return DeepLinkData(
                messageId = messageId,
                notificationId = notificationId,
                mode = mode
            )
        }
    }
}
