package com.bbuddies.madafaker.common_domain

object AppConfig {
    // Toggle this to switch between mock and real API
    const val USE_MOCK_API = false

    // Other feature flags
    const val ENABLE_LOGGING = true
    const val ENABLE_ANALYTICS = false

    const val MAX_MESSAGE_LENGTH = 1000
}