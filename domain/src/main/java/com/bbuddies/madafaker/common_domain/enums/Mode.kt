package com.bbuddies.madafaker.common_domain.enums

enum class Mode(val displayName: String, val apiValue: String) {
    SHINE("Shine", "light"),
    SHADOW("Shadow", "dark");

    companion object {
        val DEFAULT_MODE = SHINE

        fun fromApiValue(apiValue: String): Mode {
            return when (apiValue.trim().lowercase()) {
                "light", "shine" -> SHINE
                "dark", "shadow" -> SHADOW
                else -> SHINE // Default fallback
            }
        }
    }
}
