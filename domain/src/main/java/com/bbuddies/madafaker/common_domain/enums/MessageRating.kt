package com.bbuddies.madafaker.common_domain.enums

import kotlinx.serialization.Serializable

@Serializable
enum class MessageRating(val apiValue: String) {
    DISLIKE("dislike"),
    LIKE("like"),
    SUPERLIKE("superlike");

    companion object {
        fun fromApiValue(apiValue: String): MessageRating? {
            return entries.find { it.apiValue == apiValue }
        }
    }
}
