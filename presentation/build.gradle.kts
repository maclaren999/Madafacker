plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.compose.compiler)
    id(libs.plugins.daggerHilt.get().pluginId)
    alias(libs.plugins.ksp)
}

// Load unified secrets configuration
val loadSecretsConfig = rootProject.extra["loadSecretsConfig"] as (String) -> Map<String, Any>
val debugSecrets = loadSecretsConfig("debug")
val releaseSecrets = loadSecretsConfig("release")

android {
    namespace = "com.bbuddies.madafaker.presentation"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        debug {
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
}

dependencies {
    implementation(project(":domain"))
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.hilt)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.material)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.firebase.messaging.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.timber)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services)
    implementation(libs.google.identity)

    // Firebase Auth
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)

}