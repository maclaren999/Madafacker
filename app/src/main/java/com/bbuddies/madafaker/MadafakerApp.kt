package com.bbuddies.madafaker

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltAndroidApp
class MadafakerApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging FIRST
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // === FIREBASE PERSISTENCE DIAGNOSTICS ===
        Timber.tag("FIREBASE_DEBUG").d("=== Firebase Persistence Debug ===")

        // Check if Firebase shared prefs exist
        val sharedPrefsDir = File(filesDir.parent, "shared_prefs")
        val allPrefsFiles = sharedPrefsDir.listFiles()
        Timber.tag("FIREBASE_DEBUG")
            .d("All SharedPrefs files: ${allPrefsFiles?.map { "${it.name} (${it.length()} bytes)" }}")

        val firebaseFiles = allPrefsFiles?.filter {
            it.name.contains("firebase", ignoreCase = true) ||
                    it.name.contains("google", ignoreCase = true) ||
                    it.name.contains("fiam", ignoreCase = true) // Firebase In-App Messaging
        }
        Timber.tag("FIREBASE_DEBUG")
            .d("Firebase-related prefs files: ${firebaseFiles?.map { "${it.name} (${it.length()} bytes)" }}")

        // Check the specific Firebase Auth storage file
        val firebaseAuthFile = File(sharedPrefsDir, "com.google.firebase.auth.api.Store.madafaker-43c30.xml")
        Timber.tag("FIREBASE_DEBUG").d("Firebase Auth Store file exists: ${firebaseAuthFile.exists()}")
        if (firebaseAuthFile.exists()) {
            Timber.tag("FIREBASE_DEBUG").d("Firebase Auth Store file size: ${firebaseAuthFile.length()} bytes")
            try {
                val content = firebaseAuthFile.readText()
                // Log a sanitized version (don't log actual tokens)
                val hasRefreshToken = content.contains("refresh_token") || content.contains("refreshToken")
                val hasIdToken = content.contains("id_token") || content.contains("idToken")
                val hasUser = content.contains("user") || content.contains("uid")
                Timber.tag("FIREBASE_DEBUG")
                    .d("Firebase Auth Store contains: refreshToken=$hasRefreshToken, idToken=$hasIdToken, user=$hasUser")
            } catch (e: Exception) {
                Timber.tag("FIREBASE_DEBUG").e(e, "Failed to read Firebase Auth Store file")
            }
        }

        // Check for alternative Firebase Auth storage locations
        val alternateAuthFile = File(sharedPrefsDir, "com.google.firebase.auth.api.Store.xml")
        Timber.tag("FIREBASE_DEBUG").d("Alternate Firebase Auth Store exists: ${alternateAuthFile.exists()}")

        // Check Firebase app data directories
        val firebaseDir = File(filesDir, "firebase")
        Timber.tag("FIREBASE_DEBUG").d("Firebase app dir exists: ${firebaseDir.exists()}")
        if (firebaseDir.exists()) {
            Timber.tag("FIREBASE_DEBUG").d("Firebase app dir contents: ${firebaseDir.listFiles()?.map { it.name }}")
        }

        Timber.tag("FIREBASE_DEBUG").d("==================================")

        // Log Firebase Auth state IMMEDIATELY on app start
        val firebaseAuth = FirebaseAuth.getInstance()

        // Test: Add an AuthStateListener BEFORE checking currentUser
        // to see if there's any async behavior
        var listenerCallCount = 0
        firebaseAuth.addAuthStateListener { auth ->
            listenerCallCount++
            Timber.tag("APP_INIT")
                .d("Early AuthStateListener callback #$listenerCallCount: user=${auth.currentUser?.uid}")
        }

        val currentUser = firebaseAuth.currentUser
        Timber.tag("APP_INIT").d("=== MadafakerApp.onCreate() ===")
        Timber.tag("APP_INIT").d("Timestamp: ${System.currentTimeMillis()}")
        Timber.tag("APP_INIT").d("Firebase Auth currentUser: ${currentUser?.uid}")
        Timber.tag("APP_INIT").d("Firebase Auth currentUser email: ${currentUser?.email}")
        Timber.tag("APP_INIT").d("Firebase Auth currentUser isAnonymous: ${currentUser?.isAnonymous}")

        // Also check FirebaseAuth internal settings
        Timber.tag("APP_INIT").d("FirebaseAuth languageCode: ${firebaseAuth.languageCode}")
        Timber.tag("APP_INIT").d("FirebaseAuth pendingAuthResult: ${firebaseAuth.pendingAuthResult}")

        if (currentUser != null) {
            Timber.tag("APP_INIT").d("Firebase Auth lastSignIn: ${currentUser.metadata?.lastSignInTimestamp}")
            Timber.tag("APP_INIT").d("Firebase Auth creation: ${currentUser.metadata?.creationTimestamp}")
            Timber.tag("APP_INIT").d("Firebase Auth providers: ${currentUser.providerData.map { it.providerId }}")

            // Try to get token to verify session is valid
            currentUser.getIdToken(false).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.tag("APP_INIT").d("Token fetch SUCCESS - token length: ${task.result?.token?.length}")
                } else {
                    Timber.tag("APP_INIT").e(task.exception, "Token fetch FAILED")
                }
            }
        } else {
            Timber.tag("APP_INIT").w("Firebase Auth has NO USER on app start!")
        }
        Timber.tag("APP_INIT").d("===============================")

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
