package com.bbuddies.madafaker.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import local.MadafakerDatabase


@Module
@InstallIn(SingletonComponent::class)
class LocalDataModule {

    @Singleton
    @Provides
    fun providMadafakerDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        MadafakerDatabase::class.java,
        "madafaker_db"
    ).build() // The reason we can construct a database for the repo

    @Singleton
    @Provides
    fun provideYourDao(db: MadafakerDatabase) = db.getMadafakerDao()
}

