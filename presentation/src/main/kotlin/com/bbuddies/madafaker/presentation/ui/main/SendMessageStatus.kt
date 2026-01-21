package com.bbuddies.madafaker.presentation.ui.main

sealed interface SendMessageStatus {
    data object Idle : SendMessageStatus
    data object Sending : SendMessageStatus
    data object Success : SendMessageStatus
    data class Error(
        val message: String? = null,
        val errorCode: String? = null
    ) : SendMessageStatus
}
