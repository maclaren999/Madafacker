package com.bbuddies.madafaker.presentation.di

import android.content.Context
import com.bbuddies.madafaker.presentation.utils.NotificationPermissionHelper
import com.bbuddies.madafaker.presentation.utils.SharedTextManager
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

    @Provides
    @Singleton
    fun provideSharedTextManager(): SharedTextManager {
        return SharedTextManager()
    }

    @Provides
    @Singleton
    fun provideClipboardManager(@ApplicationContext context: Context): com.bbuddies.madafaker.presentation.utils.ClipboardManager {
        return com.bbuddies.madafaker.presentation.utils.ClipboardManager(context)
    }
}