package com.bbuddies.madafaker.common_domain.enums

enum class Mode(val displayName: String, val apiValue: String) {
    SHINE("Shine", "light"),
    SHADOW("Shadow", "dark");

    companion object {
        val DEFAULT_MODE = SHINE

        fun fromApiValue(apiValue: String): Mode {
            return when (apiValue.uppercase()) {
                "light" -> SHINE
                "dark" -> SHADOW
                else -> SHINE // Default fallback
            }
        }
    }
}