plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id(libs.plugins.daggerHilt.get().pluginId)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

// Load unified secrets configuration
val loadSecretsConfig = rootProject.extra["loadSecretsConfig"] as (String) -> Map<String, Any>
val debugSecrets = loadSecretsConfig("debug")
val releaseSecrets = loadSecretsConfig("release")

val majorVersion = 0
val minorVersion = 0
val patchVersion = 2

android {
    namespace = "com.bbuddies.madafaker"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.bbuddies.madafaker"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.compileSdk.get().toInt()
        versionCode = majorVersion * 10000 + minorVersion * 100 + patchVersion
        versionName = "$majorVersion.$minorVersion.$patchVersion"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        getByName("debug") {
            keyAlias = debugSecrets["DEBUG_KEYSTORE_ALIAS"]?.toString() ?: "androiddebugkey"
            keyPassword = debugSecrets["DEBUG_KEY_PASSWORD"]?.toString() ?: "android"
            storeFile = file("${projectDir}/debug.keystore")
            storePassword = debugSecrets["DEBUG_KEYSTORE_PASSWORD"]?.toString() ?: "android"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
            // Only add safe values to BuildConfig (not keystore credentials)
            buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${debugSecrets["GOOGLE_WEB_CLIENT_ID"] ?: ""}\"")
            buildConfigField("String", "API_BASE_URL", "\"${debugSecrets["API_BASE_URL"] ?: ""}\"")
        }

        release {
            // Only add safe values to BuildConfig (not keystore credentials)
            buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${releaseSecrets["GOOGLE_WEB_CLIENT_ID"] ?: ""}\"")
            buildConfigField("String", "API_BASE_URL", "\"${releaseSecrets["API_BASE_URL"] ?: ""}\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":presentation"))
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Firebase BOM
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.config)

    implementation(libs.work.runtime.ktx)
    implementation(libs.firebase.crashlytics)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.hilt)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.work)
    implementation(libs.androidx.compose.runtime)
    debugImplementation(libs.chucker)
    releaseImplementation(libs.chuckerNoOp)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services)
    implementation(libs.google.identity)
}

