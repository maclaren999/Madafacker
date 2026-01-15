package com.bbuddies.madafaker.di

import android.content.Context
import com.bbuddies.madafaker.BuildConfig
import com.bbuddies.madafaker.common_domain.AppConfig
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
     * Provides ChuckerInterceptor for HTTP request/response inspection.
     * Only active in debug builds; no-op in release builds.
     */
    @Provides
    @Singleton
    fun provideChuckerInterceptor(@ApplicationContext context: Context): ChuckerInterceptor {
        // Create a Chucker collector with custom retention policy
        val chuckerCollector = ChuckerCollector(
            context = context,
            showNotification = true,
            retentionPeriod = RetentionManager.Period.ONE_HOUR
        )

        return ChuckerInterceptor.Builder(context)
            .collector(chuckerCollector)
            .maxContentLength(250_000L)
            .redactHeaders("Authorization", "Cookie")
            .alwaysReadResponseBody(true)
            .build()
    }

    /**
     * OkHttpClient instance with interceptors for authentication, logging, and debugging.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        mockInterceptor: MockInterceptor,
        chuckerInterceptor: ChuckerInterceptor
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)

        // Only add mock interceptor if enabled
        if (AppConfig.USE_MOCK_API) {
            builder.addInterceptor(mockInterceptor)
        }

        // Add Chucker interceptor for HTTP inspection (no-op in release)
        builder.addInterceptor(chuckerInterceptor)

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