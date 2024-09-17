package com.bbuddies.madafaker.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import remote.api.BASE_URL.API_BASE_URL
import remote.api.MadafakerApi
import remote.api.interceptors.AuthInterceptor
import remote.api.logging.HttpLoggingInterceptor
import remote.api.logging.createChuckerInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

/**
 * Dagger module to provide network-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    /**
     * OkHttpClient instance.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        @ApplicationContext context: Context
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor())
            .addInterceptor(authInterceptor)
            .addInterceptor(createChuckerInterceptor(context))
            .build()

    /**
     * Provides a singleton instance of [MadafakerApi].
     *
     * @return An instance of [MadafakerApi].
     */
    @Provides
    @Singleton
    fun provideMadafakerApi(okHttpClient: OkHttpClient): MadafakerApi =
        Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(MadafakerApi::class.java)

}