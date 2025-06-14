package com.bbuddies.madafaker.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import androidx.work.WorkManager
import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import com.bbuddies.madafaker.common_domain.utils.NetworkConnectivityMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import local.MadafakerDatabase
import local.PreferenceManagerImpl
import utils.NetworkConnectivityMonitorImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class LocalDataModule {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "MF_DATA_STORE"
    )

    @Provides
    @Singleton
    fun bindPreferenceManager(preferenceManagerImpl: PreferenceManagerImpl): PreferenceManager =
        preferenceManagerImpl

    @Provides
    @Singleton
    fun providePreferenceDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore

    @Provides
    @Singleton
    fun provideMadafakerDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        MadafakerDatabase::class.java,
        "madafaker_db"
    ).build()

    @Provides
    @Singleton
    fun provideYourDao(db: MadafakerDatabase) = db.getMadafakerDao()

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)

    @Provides
    @Singleton
    fun provideNetworkConnectivityMonitor(
        @ApplicationContext context: Context
    ): NetworkConnectivityMonitor = NetworkConnectivityMonitorImpl(context)
}
