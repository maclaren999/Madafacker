plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id(libs.plugins.daggerHilt.get().pluginId)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.gms.google.services)
}

val loadBuildConfigForBuildType = rootProject.extra["loadBuildConfigForBuildType"] as (String) -> Map<String, String>

android {
    namespace = "com.bbuddies.madafaker"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.bbuddies.madafaker"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.compileSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            val config = loadBuildConfigForBuildType("debug")
            //noinspection WrongGradleMethod
            config.forEach { (key, value) ->
                buildConfigField("String", key, "\"$value\"")
            }
        }

        release {
            val config = loadBuildConfigForBuildType("release")
            //noinspection WrongGradleMethod
            config.forEach { (key, value) ->
                buildConfigField("String", key, "\"$value\"")
            }
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
    implementation(libs.firebase.config)

    implementation(libs.work.runtime.ktx)
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

