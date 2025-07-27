plugins {
    id("java-library")
    alias(libs.plugins.jetbrainsKotlinJvm)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinx.serialization)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    // Room annotations for domain models
    api(libs.room.runtime)
    ksp(libs.room.compiler)

    // Kotlinx Serialization for Room type converters
    implementation(libs.kotlinx.serialization.json)

    // JSR-330 dependency injection annotations
    implementation("javax.inject:javax.inject:1")
}