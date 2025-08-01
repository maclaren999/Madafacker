import groovy.json.JsonSlurper

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id(libs.plugins.daggerHilt.get().pluginId)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.gms.google.services)
}

val loadBuildConfigForBuildType = rootProject.extra["loadBuildConfigForBuildType"] as (String) -> Map<String, String>

// Load keystore config from secrets.json
fun loadKeystoreConfig(): Map<String, String> {
    val configFile = File(projectDir, "secrets.json")
    val config = mutableMapOf<String, String>()

    if (configFile.exists()) {
        try {
            val jsonSlurper = JsonSlurper()
            val fileConfig = jsonSlurper.parse(configFile) as Map<String, Any>
            val debugConfig = fileConfig["debug"] as? Map<String, Any>
            
            if (debugConfig != null) {
                config["keyAlias"] = debugConfig["DEBUG_KEYSTORE_ALIAS"]?.toString() ?: "androiddebugkey"
                config["keyPassword"] = debugConfig["DEBUG_KEY_PASSWORD"]?.toString() ?: "android"
                config["storePassword"] = debugConfig["DEBUG_KEYSTORE_PASSWORD"]?.toString() ?: "android"
            }
        } catch (e: Exception) {
            println("Error loading keystore config: ${e.message}")
        }
    }

    // Environment variables override
    System.getenv("DEBUG_KEYSTORE_ALIAS")?.let { config["keyAlias"] = it }
    System.getenv("DEBUG_KEY_PASSWORD")?.let { config["keyPassword"] = it }
    System.getenv("DEBUG_KEYSTORE_PASSWORD")?.let { config["storePassword"] = it }

    return config
}

val keystoreConfig = loadKeystoreConfig()

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

    signingConfigs {
        getByName("debug") {
            keyAlias = keystoreConfig["keyAlias"] ?: "androiddebugkey"
            keyPassword = keystoreConfig["keyPassword"] ?: "android"
            storeFile = file("${projectDir}/debug.keystore")
            storePassword = keystoreConfig["storePassword"] ?: "android"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
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

