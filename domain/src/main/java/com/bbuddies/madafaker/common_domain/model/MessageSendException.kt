package com.bbuddies.madafaker.common_domain.model

class MessageSendException(
    val statusCode: Int? = null,
    val statusMessage: String? = null,
    val errorCode: String? = null,
    val errorMessage: String? = null,
    cause: Throwable? = null
) : Exception(errorMessage, cause)
