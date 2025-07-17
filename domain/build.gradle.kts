plugins {
    id("java-library")
    alias(libs.plugins.jetbrainsKotlinJvm)
    id(libs.plugins.kotlin.kapt.get().pluginId)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    api(libs.room.runtime)
    kapt(libs.room.compiler)
    testImplementation(kotlin("test"))

}