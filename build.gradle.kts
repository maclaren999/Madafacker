import groovy.json.JsonSlurper

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.daggerHilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsKotlinJvm) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.google.gms.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}

/**
 * Unified secrets configuration loader.
 *
 * Loads configuration from either:
 * 1. MADAFAKER_SECRETS_JSON environment variable (CI/CD)
 * 2. app/secrets.json file (local development)
 *
 * Expected JSON structure:
 * {
 *   "debug": {
 *     "GOOGLE_WEB_CLIENT_ID": "...",
 *     "API_BASE_URL": "...",
 *     "DEBUG_KEYSTORE_ALIAS": "...",
 *     "DEBUG_KEYSTORE_PASSWORD": "...",
 *     "DEBUG_KEY_PASSWORD": "..."
 *   },
 *   "release": { "KEY": "release-value" }
 * }
 *
 * @param buildType The build type name (e.g., "debug", "release")
 * @return Map of configuration key-value pairs for the specified build type
 */
extra["loadSecretsConfig"] = { buildType: String ->
    val secretsJson = System.getenv("MADAFAKER_SECRETS_JSON")

    val buildTypeConfig = if (secretsJson != null) {
        // Load from environment variable (CI/CD)
        try {
            val jsonSlurper = JsonSlurper()
            val config = jsonSlurper.parseText(secretsJson) as Map<String, Any>
            val result = config[buildType] as? Map<String, Any> ?: emptyMap()
            println("✅ Loaded secrets from environment variable (buildType: $buildType)")
            result
        } catch (e: Exception) {
            println("❌ Error parsing MADAFAKER_SECRETS_JSON: ${e.message}")
            emptyMap<String, Any>()
        }
    } else {
        // Load from file (local development)
        val configFile = File(rootDir, "app/secrets.json")
        if (configFile.exists()) {
            try {
                val jsonSlurper = JsonSlurper()
                val config = jsonSlurper.parse(configFile) as Map<String, Any>
                val result = config[buildType] as? Map<String, Any> ?: emptyMap()
                println("✅ Loaded secrets from file: app/secrets.json (buildType: $buildType)")
                result
            } catch (e: Exception) {
                println("❌ Error loading secrets file: ${e.message}")
                emptyMap<String, Any>()
            }
        } else {
            println("⚠️  Secrets file not found: app/secrets.json")
            emptyMap<String, Any>()
        }
    }

    buildTypeConfig
}