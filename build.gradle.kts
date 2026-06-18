plugins {
    kotlin("jvm") version "2.1.20-Beta2" apply false
    kotlin("multiplatform") version "2.1.20-Beta2" apply false
    kotlin("plugin.serialization") version "2.1.20-Beta2" apply false
    id("org.jetbrains.compose") version "1.7.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.20-Beta2" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}

// Ensure Kotlin JS uses a reachable Node.js distribution
plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
    extensions.configure<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension> {
        version = "20.12.2"
        downloadBaseUrl = "https://nodejs.org/dist"
    }
}
