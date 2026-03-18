plugins {
    kotlin("multiplatform") version "2.3.10"
}

group = "taniere.tetrasnake"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                outputFileName = "app.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        val jsMain by getting {
            // On garde la structure de projet existante (`src/main/kotlin`).
            kotlin.srcDir("src/main/kotlin")
        }
    }
}