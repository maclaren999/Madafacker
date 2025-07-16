package com.bbuddies.madafaker.di

import com.bbuddies.madafaker.BuildConfig
import com.bbuddies.madafaker.common_domain.AppConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import remote.api.MadafakerApi
import remote.api.interceptors.AuthInterceptor
import remote.api.interceptors.MockInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

/**
 * Dagger module to provide network-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * OkHttpClient instance.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        mockInterceptor: MockInterceptor
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)

        // Only add mock interceptor if enabled
        if (AppConfig.USE_MOCK_API) {
            builder.addInterceptor(mockInterceptor)
        }

        if (AppConfig.ENABLE_LOGGING) {
            // Add logging interceptor for debugging
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }

        return builder.build()
    }

    /**
     * Provides a singleton instance of [MadafakerApi].
     *
     * @return An instance of [MadafakerApi].
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL) // Replace with actual URL
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideMadafakerApi(retrofit: Retrofit): MadafakerApi {
        return retrofit.create(MadafakerApi::class.java)
    }
}