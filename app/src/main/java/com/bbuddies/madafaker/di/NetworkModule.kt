package com.bbuddies.madafaker.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import remote.api.Constant.API_BASE_URL
import remote.api.MadafakerApi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    private val baseRetrofitBuilder: Retrofit.Builder =
        Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())

    private val okHttpClientBuilder: OkHttpClient.Builder =
        OkHttpClient.Builder()

    @Provides
    @Singleton
    fun provideMadafakerApi(): MadafakerApi =
        baseRetrofitBuilder
            .client(okHttpClientBuilder.build())
//            .addCallAdapterFactory(CoroutineCallAdapterFactory()) //TODO: resolve if needed
            .build()
            .create(MadafakerApi::class.java)

}