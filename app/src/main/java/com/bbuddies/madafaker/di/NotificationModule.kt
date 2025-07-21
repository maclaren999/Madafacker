package com.bbuddies.madafaker.di

import com.bbuddies.madafaker.data.notification.AnalyticsRepositoryImpl
import com.bbuddies.madafaker.data.notification.NotificationRepositoryImpl
import com.bbuddies.madafaker.notification_domain.repository.AnalyticsRepository
import com.bbuddies.madafaker.notification_domain.repository.NotificationRepository
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindAnalyticsRepository(
        analyticsRepositoryImpl: AnalyticsRepositoryImpl
    ): AnalyticsRepository

    companion object {

        @Provides
        @Singleton
        fun provideFirebaseAnalytics(): FirebaseAnalytics {
            return Firebase.analytics
        }

        @Provides
        @Singleton
        fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
            val remoteConfig = Firebase.remoteConfig
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 3600 // 1 hour
            }
            remoteConfig.setConfigSettingsAsync(configSettings)

            // Set default values
            val defaults = mapOf(
                "notification_frequency_base" to 2L,
                "nighttime_start_hour" to 22L,
                "nighttime_end_hour" to 8L,
                "placeholder_messages_shine" to """[
                    "Someone shared a thought with you ✨",
                    "A stranger left you something to consider 💭",
                    "Fresh perspective from the universe 🌟",
                    "You've got a random message waiting 🎲",
                    "Someone reached out to you ☀️",
                    "A soul dropped a line for you 📝",
                    "Something meaningful just arrived 🌸",
                    "A human moment awaits ⚡"
                ]""",
                "placeholder_messages_shadow" to """[
                    "Someone shared their unfiltered thoughts 🌙",
                    "A wild message appeared from the shadows 🎪",
                    "Raw thoughts from a stranger 🔮",
                    "Someone dropped their guard for you 🎯",
                    "Unfiltered wisdom just landed 🌊",
                    "A stranger's honest take awaits 🎭",
                    "Something real just surfaced 🌑",
                    "Truth from the underground 🔥"
                ]"""
            )
            remoteConfig.setDefaultsAsync(defaults)

            return remoteConfig
        }


    }
}
