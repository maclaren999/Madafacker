import groovy.json.JsonSlurper

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.daggerHilt) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsKotlinJvm) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.google.gms.google.services) apply false
}

/**
 * Loads build configuration values from a JSON file based on build type.
 *
 * Expected JSON structure:
 * {
 *   "debug": { "KEY": "debug-value" },
 *   "release": { "KEY": "release-value" }
 * }
 *
 * File location:
 * - Default: app/secrets.json (relative to project root)
 * - Custom: Set MADAFAKER_SECRETS_PATH environment variable
 *
 * Security:
 * - Only whitelisted keys are loaded from JSON
 * - Environment variables can override file values
 *
 * Usage:
 * ```kotlin
 * val config = loadBuildConfigForBuildType("debug")
 * config.forEach { (key, value) ->
 *     buildConfigField("String", key, "\"$value\"")
 * }
 * ```
 *
 * @param buildType The build type name (e.g., "debug", "release")
 * @return Map of configuration key-value pairs for the specified build type
 */
extra["loadBuildConfigForBuildType"] = { buildType: String ->
    val config = mutableMapOf<String, String>()

    // Whitelist of allowed keys for security
    val allowedKeys = setOf(
        "GOOGLE_WEB_CLIENT_ID",
        "API_BASE_URL",
    )

    // Use environment variable or fallback to default path
    val configPath = System.getenv("MADAFAKER_SECRETS_PATH") ?: "app/secrets.json"
    val configFile = File(rootDir, configPath)

    // Load from file
    if (configFile.exists()) {
        try {
            val jsonSlurper = JsonSlurper()
            val fileConfig = jsonSlurper.parse(configFile) as Map<String, Any>

            // Get build type specific config
            val buildTypeConfig = fileConfig[buildType] as? Map<String, Any>
            if (buildTypeConfig != null) {
                buildTypeConfig.forEach { (key, value) ->
                    if (key in allowedKeys) {
                        config[key] = value.toString()
                    } else println("❌ Error in config file: $key is not in allowedKeys collection")
                }
                println("✅ Loaded config from: $configPath (buildType: $buildType)")
            } else {
                println("⚠️  No config found for buildType '$buildType' in $configPath")
            }
        } catch (e: Exception) {
            println("❌ Error loading config file: ${e.message}")
        }
    } else {
        println("⚠️  Config file not found: $configPath")
    }

    // Environment variables can still override (useful for CI/CD)
    allowedKeys.forEach { key ->
        System.getenv(key)?.let { value ->
            config[key] = value
            println("🔄 Overriding $key with environment variable")
        }
    }

    config
}