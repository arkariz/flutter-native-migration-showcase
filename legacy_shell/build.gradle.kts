plugins {
    id("com.android.application") version "9.5.1" apply false
    // :app has no separate Kotlin Android plugin — AGP 9's built-in Kotlin
    // support handles it (brings its own Kotlin Gradle Plugin runtime dep).
    kotlin("jvm") version "2.4.21" apply false
}
