package com.bbuddies.madafaker.common_domain.model

data class UnsentDraft(
    val body: String,
    val mode: String,
    val timestamp: Long
)