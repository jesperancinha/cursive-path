plugins {
    kotlin("multiplatform")
}

kotlin {
    js(IR) {
        browser {
            binaries.executable()
        }
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation("org.jetbrains.compose.web:web-core:1.6.10")
                implementation("org.jetbrains.compose.runtime:runtime:1.6.10")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
            }
        }
    }
}
