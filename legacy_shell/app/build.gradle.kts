plugins {
    id("com.android.application")
    // No `kotlin("android")` plugin: AGP 9's built-in Kotlin support compiles
    // Kotlin sources directly — see https://kotl.in/gradle/agp-built-in-kotlin.
}

android {
    namespace = "com.example.legacyshell"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.legacyshell"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    // AGP 9's built-in Kotlin support derives the Kotlin JVM target from
    // compileOptions above — no separate `kotlinOptions`/`kotlin {}` block.
}

dependencies {
    implementation(project(":bridge-core"))

    // Real add-to-app integration depends on the Flutter engine embedding classes
    // (io.flutter.embedding.android.*), published to Flutter's own Maven repo at
    // https://storage.googleapis.com/download.flutter.io under a coordinate pinned
    // to the exact Flutter SDK's engine build (e.g. "io.flutter:flutter_embedding_debug:1.0.0-<engine-hash>").
    // That coordinate is produced by running `flutter build aar` with a real Flutter
    // SDK, which this repo's CI deliberately doesn't do — see README.md and
    // bridge-core's CI job, which covers the tested logic without needing it.
    // implementation("io.flutter:flutter_embedding_debug:1.0.0-<engine-hash>")
}
