package com.bbuddies.madafaker.presentation.di

import com.bbuddies.madafaker.common_domain.auth.TokenRefreshService
import com.bbuddies.madafaker.presentation.auth.GoogleAuthManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    abstract fun bindTokenRefreshService(
        googleAuthManager: GoogleAuthManager
    ): TokenRefreshService
}
