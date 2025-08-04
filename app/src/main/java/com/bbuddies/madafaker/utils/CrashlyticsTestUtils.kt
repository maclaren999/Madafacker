package com.bbuddies.madafaker.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * Utility class for testing Firebase Crashlytics functionality.
 * This should only be used for testing purposes.
 */
object CrashlyticsTestUtils {

    /**
     * Forces a test crash to verify Crashlytics is working properly.
     * This should only be called in debug builds for testing.
     */
    fun forceTestCrash() {
        Timber.d("Forcing test crash for Crashlytics verification")
        throw RuntimeException("Test crash for Firebase Crashlytics setup verification")
    }

    /**
     * Logs a non-fatal error to test Crashlytics logging.
     */
    fun logTestError() {
        val testException = Exception("Test non-fatal error for Crashlytics")
        FirebaseCrashlytics.getInstance().recordException(testException)
        Timber.d("Logged test non-fatal error to Crashlytics")
    }

    /**
     * Sets custom keys for crash reports to help with debugging.
     */
    fun setTestCustomKeys() {
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.setCustomKey("test_setup", "crashlytics_integration")
        crashlytics.setCustomKey("app_version", com.bbuddies.madafaker.BuildConfig.VERSION_NAME)
        crashlytics.setCustomKey("debug_build", com.bbuddies.madafaker.BuildConfig.DEBUG)
        Timber.d("Set custom keys for Crashlytics testing")
    }

    /**
     * Logs a custom message to Crashlytics.
     */
    fun logTestMessage(message: String) {
        FirebaseCrashlytics.getInstance().log("Test message: $message")
        Timber.d("Logged custom message to Crashlytics: $message")
    }
}
