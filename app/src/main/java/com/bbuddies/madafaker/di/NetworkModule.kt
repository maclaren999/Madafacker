package com.bbuddies.madafaker.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import remote.api.BASE_URL.API_BASE_URL
import remote.api.MadafakerApi
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
     * Retrofit builder configured with the base URL and Moshi converter factory.
     */
    private val baseRetrofitBuilder: Retrofit.Builder =
        Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())

    /**
     * OkHttpClient builder for creating OkHttpClient instances.
     */
    private val okHttpClientBuilder: OkHttpClient.Builder =
        OkHttpClient.Builder()

    /**
     * Provides a singleton instance of [MadafakerApi].
     *
     * @return An instance of [MadafakerApi].
     */
    @Provides
    @Singleton
    fun provideMadafakerApi(): MadafakerApi =
        baseRetrofitBuilder
            .client(okHttpClientBuilder.build())
            .build()
            .create(MadafakerApi::class.java)

}