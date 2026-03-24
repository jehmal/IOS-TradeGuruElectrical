pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
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
    versionCatalogs {
        create("libs") {
            // Plugins
            version("agp", "8.7.3")
            version("kotlin", "2.0.21")
            version("ksp", "2.0.21-1.0.28")
            version("room", "2.6.1")

            plugin("android-application", "com.android.application").versionRef("agp")
            plugin("kotlin-android", "org.jetbrains.kotlin.android").versionRef("kotlin")
            plugin("kotlin-compose", "org.jetbrains.kotlin.plugin.compose").versionRef("kotlin")
            plugin("ksp", "com.google.devtools.ksp").versionRef("ksp")
            plugin("room", "androidx.room").versionRef("room")

            // Compose BOM
            version("composeBom", "2024.12.01")
            library("compose-bom", "androidx.compose", "compose-bom").versionRef("composeBom")
            library("compose-ui", "androidx.compose.ui", "ui").withoutVersion()
            library("compose-ui-graphics", "androidx.compose.ui", "ui-graphics").withoutVersion()
            library("compose-ui-tooling", "androidx.compose.ui", "ui-tooling").withoutVersion()
            library("compose-ui-tooling-preview", "androidx.compose.ui", "ui-tooling-preview").withoutVersion()
            library("compose-material3", "androidx.compose.material3", "material3").withoutVersion()
            library("compose-material-icons-extended", "androidx.compose.material", "material-icons-extended").withoutVersion()

            // Navigation
            version("composeNavigation", "2.8.5")
            library("compose-navigation", "androidx.navigation", "navigation-compose").versionRef("composeNavigation")

            // Lifecycle
            version("lifecycle", "2.8.7")
            library("lifecycle-viewmodel-compose", "androidx.lifecycle", "lifecycle-viewmodel-compose").versionRef("lifecycle")
            library("lifecycle-runtime-compose", "androidx.lifecycle", "lifecycle-runtime-compose").versionRef("lifecycle")

            // Room
            library("room-runtime", "androidx.room", "room-runtime").versionRef("room")
            library("room-ktx", "androidx.room", "room-ktx").versionRef("room")
            library("room-compiler", "androidx.room", "room-compiler").versionRef("room")

            // Coroutines
            version("coroutines", "1.9.0")
            library("coroutines-android", "org.jetbrains.kotlinx", "kotlinx-coroutines-android").versionRef("coroutines")

            // OkHttp
            version("okhttp", "4.12.0")
            library("okhttp", "com.squareup.okhttp3", "okhttp").versionRef("okhttp")

            // Browser (Chrome Custom Tabs)
            library("browser", "androidx.browser", "browser").version("1.8.0")

            // Gson
            library("gson", "com.google.code.gson", "gson").version("2.11.0")

            // Coil
            version("coil", "2.7.0")
            library("coil-compose", "io.coil-kt", "coil-compose").versionRef("coil")

            // DataStore
            library("datastore-preferences", "androidx.datastore", "datastore-preferences").version("1.1.1")

            // CameraX
            version("camerax", "1.4.1")
            library("camerax-core", "androidx.camera", "camera-core").versionRef("camerax")
            library("camerax-camera2", "androidx.camera", "camera-camera2").versionRef("camerax")
            library("camerax-lifecycle", "androidx.camera", "camera-lifecycle").versionRef("camerax")
            library("camerax-view", "androidx.camera", "camera-view").versionRef("camerax")

            // Security
            library("security-crypto", "androidx.security", "security-crypto").version("1.1.0-alpha06")

            // Accompanist
            library("accompanist-permissions", "com.google.accompanist", "accompanist-permissions").version("0.36.0")

            // Core
            library("core-ktx", "androidx.core", "core-ktx").version("1.15.0")
            library("activity-compose", "androidx.activity", "activity-compose").version("1.9.3")

            // Testing
            library("junit", "junit", "junit").version("4.13.2")
            library("compose-ui-test-junit4", "androidx.compose.ui", "ui-test-junit4").withoutVersion()
            library("compose-ui-test-manifest", "androidx.compose.ui", "ui-test-manifest").withoutVersion()
        }
    }
}

rootProject.name = "TradeGuruElectrical"
include(":app")
