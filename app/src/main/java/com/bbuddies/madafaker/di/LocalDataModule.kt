package com.bbuddies.madafaker.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import local.PreferenceManagerImpl


@Module
@InstallIn(SingletonComponent::class)
abstract class LocalDataModule {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "MF_DATA_STORE"
    )

    @Binds
    abstract fun bindPreferenceManager(preferenceManagerImpl: PreferenceManagerImpl): PreferenceManager

    @Provides
    fun providePreferenceDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore


}