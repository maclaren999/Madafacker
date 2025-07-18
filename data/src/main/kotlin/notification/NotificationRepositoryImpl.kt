package com.bbuddies.madafaker.data.notification

import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.notification_domain.repository.NotificationRepository
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig
) : NotificationRepository {

    override suspend fun getPlaceholderMessages(mode: Mode): List<String> {
        return try {
            // Fetch latest config
            remoteConfig.fetchAndActivate().await()

            val configKey = when (mode) {
                Mode.SHINE -> "placeholder_messages_shine"
                Mode.SHADOW -> "placeholder_messages_shadow"
            }

            val messagesJson = remoteConfig.getString(configKey)
            if (messagesJson.isNotEmpty()) {
                // Parse JSON array of strings
                parseJsonStringArray(messagesJson)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get placeholder messages from Remote Config")
            emptyList()
        }
    }

    override suspend fun getNotificationConfig(): Map<String, Any> {
        return try {
            remoteConfig.fetchAndActivate().await()

            mapOf(
                "baseFrequency" to remoteConfig.getLong("notification_frequency_base"),
                "nighttimeStartHour" to remoteConfig.getLong("nighttime_start_hour"),
                "nighttimeEndHour" to remoteConfig.getLong("nighttime_end_hour")
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to get notification config")
            // Return default values
            mapOf(
                "baseFrequency" to 2L,
                "nighttimeStartHour" to 22L,
                "nighttimeEndHour" to 8L
            )
        }
    }

    private fun parseJsonStringArray(json: String): List<String> {
        return try {
            val jsonElement = Json.parseToJsonElement(json)
            jsonElement.jsonArray.map { it.jsonPrimitive.content }
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse JSON string array")
            emptyList()
        }
    }
}
