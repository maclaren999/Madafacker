package com.bbuddies.madafaker.presentation.di

import android.content.Context
import com.bbuddies.madafaker.presentation.utils.NotificationPermissionHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PresentationModule {

    @Provides
    @Singleton
    fun provideNotificationPermissionHelper(
        @ApplicationContext context: Context
    ): NotificationPermissionHelper {
        return NotificationPermissionHelper(context)
    }
}