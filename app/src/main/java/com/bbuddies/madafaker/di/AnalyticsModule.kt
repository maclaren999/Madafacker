package com.bbuddies.madafaker.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface AnalyticsModule {
    // Define your dependencies here
}