package com.bbuddies.madafaker.common_domain.enums

enum class Mode(val displayName: String, val apiValue: String) {
    SHINE("Shine", "LIGHT"),
    SHADOW("Shadow", "DARK");

    companion object {
        val DEFAULT_MODE = SHINE

        fun fromApiValue(apiValue: String): Mode {
            return when (apiValue.uppercase()) {
                "LIGHT" -> SHINE
                "DARK" -> SHADOW
                else -> SHINE // Default fallback
            }
        }
    }
}