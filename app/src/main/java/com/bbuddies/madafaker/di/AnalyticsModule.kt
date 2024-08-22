package com.bbuddies.madafaker.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/*
* This module is responsible for providing the analytics dependencies.
* */
@Module
@InstallIn(SingletonComponent::class)
interface AnalyticsModule {
    // Define your dependencies here
}