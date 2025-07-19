package com.bbuddies.madafaker.di

import com.bbuddies.madafaker.common_domain.repository.MessageRepository
import com.bbuddies.madafaker.common_domain.repository.UserRepository
import com.bbuddies.madafaker.notification.NotificationManager
import com.bbuddies.madafaker.notification_domain.repository.NotificationManagerRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import repository.MessageRepositoryImpl
import repository.UserRepositoryImpl

/*
* This module is responsible for providing the repository implementations.
* */
@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    fun messageRepository(repository: MessageRepositoryImpl): MessageRepository

    @Binds
    fun userRepository(repository: UserRepositoryImpl): UserRepository

    @Binds
    fun notificationManagerRepository(notificationManager: NotificationManager): NotificationManagerRepository

}