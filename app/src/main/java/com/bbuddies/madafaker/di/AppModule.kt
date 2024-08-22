package com.bbuddies.madafaker.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Dagger module to provide app-level dependencies.
 *
 * It is preferable to use @Bind when providing interfaces and @Provides when providing concrete classes.
 */
@Module
@InstallIn(SingletonComponent::class)
interface AppModule {
    // Define your dependencies here
}