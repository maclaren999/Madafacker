package com.bbuddies.madafaker.di

import com.bbuddies.madafaker.common_domain.repository.MessageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import remote.MessageRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    fun repository(repository: MessageRepositoryImpl): MessageRepository

}