package remote.api

import com.bbuddies.madafaker.common_domain.model.MessageSendException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException

internal object MessageErrorMapper {
    private val errorJson = Json { ignoreUnknownKeys = true }

    private data class ApiErrorDetails(
        val message: String? = null,
        val code: String? = null
    )

    private fun parseErrorDetails(errorBody: String?): ApiErrorDetails {
        if (errorBody.isNullOrBlank()) return ApiErrorDetails()

        return try {
            val element = errorJson.decodeFromString<JsonElement>(errorBody)
            val obj = element.jsonObject
            val message = obj["message"]?.jsonPrimitive?.contentOrNull
                ?: obj["error"]?.jsonPrimitive?.contentOrNull
                ?: obj["detail"]?.jsonPrimitive?.contentOrNull
                ?: obj["details"]?.jsonPrimitive?.contentOrNull
            val code = obj["code"]?.jsonPrimitive?.contentOrNull
                ?: obj["errorCode"]?.jsonPrimitive?.contentOrNull
                ?: obj["error_code"]?.jsonPrimitive?.contentOrNull

            ApiErrorDetails(message = message, code = code)
        } catch (e: Exception) {
            ApiErrorDetails(message = errorBody.trim())
        }
    }

    fun mapSendMessageException(exception: Exception): MessageSendException {
        return when (exception) {
            is HttpException -> {
                val statusCode = exception.code()
                val details = parseErrorDetails(exception.response()?.errorBody()?.string())
                val isClientError = statusCode in 400..499

                MessageSendException(
                    statusCode = statusCode,
                    errorCode = if (isClientError) details.code else null,
                    errorMessage = if (isClientError) details.message else null,
                    cause = exception
                )
            }

            else -> MessageSendException(
                errorMessage = null,
                cause = exception
            )
        }
    }
}
