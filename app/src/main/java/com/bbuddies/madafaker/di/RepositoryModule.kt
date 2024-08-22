package com.bbuddies.madafaker.di

import com.bbuddies.madafaker.common_domain.repository.MessageRepository
import com.bbuddies.madafaker.common_domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import remote.MessageRepositoryImpl
import remote.UserRepositoryImpl

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

}