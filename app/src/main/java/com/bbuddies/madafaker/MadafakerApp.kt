package com.bbuddies.madafaker

import android.app.Application
import android.os.Build
import android.os.Process
import android.os.UserManager
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
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

        val userManager = getSystemService(UserManager::class.java)
        val isUserUnlocked = userManager?.isUserUnlocked ?: true
        Timber.tag("FIREBASE_DEBUG")
            .d("DirectBoot: isDeviceProtectedStorage=$isDeviceProtectedStorage, isUserUnlocked=$isUserUnlocked")
        Timber.tag("FIREBASE_DEBUG")
            .d("Process: pid=${Process.myPid()} uid=${Process.myUid()}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Timber.tag("FIREBASE_DEBUG").d("Process name: ${Application.getProcessName()}")
        }
        Timber.tag("FIREBASE_DEBUG").d("DataDir: ${applicationInfo.dataDir}")

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

        // Check Firebase Auth storage files (actual filename includes encoded app info)
        val firebaseAuthStoreFiles = allPrefsFiles?.filter {
            it.name.startsWith("com.google.firebase.auth.api.Store")
        }
        Timber.tag("FIREBASE_DEBUG")
            .d("Firebase Auth Store files: ${firebaseAuthStoreFiles?.map { "${it.name} (${it.length()} bytes)" }}")
        firebaseAuthStoreFiles?.firstOrNull()?.let { firebaseAuthFile ->
            Timber.tag("FIREBASE_DEBUG").d("Firebase Auth Store lastModified: ${firebaseAuthFile.lastModified()}")
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

        val firebaseAuthCryptoFiles = allPrefsFiles?.filter {
            it.name.startsWith("com.google.firebase.auth.api.crypto")
        }
        Timber.tag("FIREBASE_DEBUG")
            .d("Firebase Auth Crypto files: ${firebaseAuthCryptoFiles?.map { "${it.name} (${it.length()} bytes)" }}")

        // Check DataStore file (session state + cached tokens)
        val dataStoreFile = File(filesDir, "datastore/MF_DATA_STORE.preferences_pb")
        Timber.tag("FIREBASE_DEBUG")
            .d("DataStore file exists: ${dataStoreFile.exists()} (${dataStoreFile.length()} bytes)")

        // Check Firebase app data directories
        val firebaseDir = File(filesDir, "firebase")
        Timber.tag("FIREBASE_DEBUG").d("Firebase app dir exists: ${firebaseDir.exists()}")
        if (firebaseDir.exists()) {
            Timber.tag("FIREBASE_DEBUG").d("Firebase app dir contents: ${firebaseDir.listFiles()?.map { it.name }}")
        }

        Timber.tag("FIREBASE_DEBUG").d("==================================")

        // Log Firebase Auth state IMMEDIATELY on app start
        val firebaseAuth = FirebaseAuth.getInstance()
        try {
            val apps = FirebaseApp.getApps(this)
            Timber.tag("FIREBASE_DEBUG").d("Firebase apps: ${apps.map { it.name }}")
            val options = FirebaseApp.getInstance().options
            Timber.tag("FIREBASE_DEBUG")
                .d("Firebase options: appId=${options.applicationId}, projectId=${options.projectId}, storageBucket=${options.storageBucket}")
        } catch (e: Exception) {
            Timber.tag("FIREBASE_DEBUG").w(e, "Failed to read FirebaseApp options")
        }

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
