pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "legacy-shell"

// Pure-Kotlin/JVM module: the testable bridge logic (no Android SDK needed to build/test it).
include(":bridge-core")

// The actual Android app: Activities, SharedPreferences-backed session store, and the
// real Flutter engine wiring. See app/README.md for why this module isn't built by this
// repo's CI (it needs a Flutter engine artifact only `flutter build aar` can produce).
include(":app")
