package com.bbuddies.madafaker.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import local.MadafakerDatabase
import local.PreferenceManagerImpl


@Module
@InstallIn(SingletonComponent::class)
class LocalDataModule {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "MF_DATA_STORE"
    )

    @Provides
    fun bindPreferenceManager(preferenceManagerImpl: PreferenceManagerImpl): PreferenceManager =
        preferenceManagerImpl

    @Provides
    fun providePreferenceDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore


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
