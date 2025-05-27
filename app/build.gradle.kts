import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.compose)
    kotlin("plugin.serialization") version embeddedKotlinVersion
    alias(libs.plugins.maps.secrets)
    alias(libs.plugins.google.services)
    alias(libs.plugins.devtools.ksp)
}

val localProperties = Properties()
localProperties.load(FileInputStream(rootProject.file("local.properties")))

android {
    namespace = "com.example.nexttransit"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.nexttransit"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        buildConfigField("String", "API_KEY",localProperties.getProperty("API_KEY"))

        ksp {
            arg("room.schemaLocation","$projectDir/schemas")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.play.services.maps)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
//    ┌───────────────────────────────────┐
//    │Native UI library - Jetpack Compose│
//    └───────────────────────────────────┘
    val composeBom = platform("androidx.compose:compose-bom:2025.03.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    // Choose one of the following:
    // Material Design 3
    implementation(libs.androidx.material3)
    // Android Studio Preview support
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
    // UI Tests
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)
    // Optional - Add full set of material icons
    implementation(libs.androidx.material.icons.extended)
    // Optional - Add window size utils
    implementation(libs.androidx.material3.window.size.class1)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.activity.compose)


//    ┌────────────────────────────────────────────┐
//    │  Data persistence library - Room (SQLite)  │
//    └────────────────────────────────────────────┘
    implementation(libs.androidx.room.runtime)
    // Kotlin Symbol Processing (KSP) for annotations
    ksp(libs.androidx.room.compiler)
    // optional - Kotlin Extensions and Coroutines support for Room
    implementation(libs.androidx.room.ktx)


//    ┌───────────────────────────────────────────────┐
//    │  Data persistence library - Proto DataStore   │
//    └───────────────────────────────────────────────┘
    // DataStore to save user preferences (as immutable json dump)
    implementation(libs.androidx.datastore)
    implementation(libs.protobuf.gradle.plugin)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)


//    ┌─────────────────────────────────────────┐
//    │ Networking library - Ktor + dependencies│
//    └─────────────────────────────────────────┘
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)


//    ┌─────────────────────────────────────┐
//    │ Home screen widget library - Glance │
//    └─────────────────────────────────────┘
    // For Glance support
    implementation(libs.androidx.glance)
    // For AppWidgets support
    implementation(libs.androidx.glance.appwidget)
    // For interop APIs with Material 3
    implementation(libs.androidx.glance.material3)


    // Jetpack Compose Navigation
    implementation(libs.androidx.navigation.compose)
    androidTestImplementation(libs.androidx.navigation.testing)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.material3.adaptive.navigation.suite)

//    ┌─────────────────────────────────────┐
//    │       Firebase Cloud Messaging      │
//    └─────────────────────────────────────┘
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)


//    ┌─────────────────────────────────────┐
//    │       Firebase  Authentication      │
//    └─────────────────────────────────────┘
    implementation(libs.firebase.ui.auth)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

}
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
