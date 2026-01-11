package com.bbuddies.madafaker

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class MadafakerApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Initialize Firebase Crashlytics
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true) //TODO: !BuildConfig.DEBUG

        // Set user identifier for crash reports (will be updated when user logs in)
        FirebaseCrashlytics.getInstance().setUserId("anonymous")

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Timber.tag("FCM").w(task.exception, "Failed to fetch FCM token on app start")
                return@addOnCompleteListener
            }

            Timber.tag("FCM").d("Current FCM token: ${task.result}")
        }

        // Log app initialization
        Timber.d("MadafakerApp initialized with Crashlytics enabled: ${!BuildConfig.DEBUG}")
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
