import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("plugin.serialization") version embeddedKotlinVersion
//    kotlin("kapt")
//    id("com.google.dagger.hilt.android")
}

val localProperties = Properties()
localProperties.load(FileInputStream(rootProject.file("local.properties")))

android {
    namespace = "com.example.nexttransit"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.nexttransit"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        buildConfigField("String", "API_KEY",localProperties.getProperty("API_KEY"))
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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    //    ┌───────────────────────────────────┐
//    │Native UI library - Jetpack Compose│
//    └───────────────────────────────────┘
    val composeBom = platform("androidx.compose:compose-bom:2024.02.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    // Choose one of the following:
    // Material Design 3
    implementation("androidx.compose.material3:material3:1.2.1")
    // Android Studio Preview support
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    // UI Tests
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.3")
    // Optional - Add full set of material icons
    implementation("androidx.compose.material:material-icons-extended")
    // Optional - Add window size utils
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.activity:activity-compose:1.8.2")

    // DataStore to save user preferences
    implementation("androidx.datastore:datastore:1.1.0-beta02")
    implementation("com.google.protobuf:protobuf-gradle-plugin:0.9.4")

    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.7")


//    ┌─────────────────────────────────────────┐
//    │ Networking library - Ktor + dependencies│
//    └─────────────────────────────────────────┘
    val ktorVersion = "2.3.8"
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-android:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")


//    ┌─────────────────────────────────────┐
//    │ Home screen widget library - Glance │
//    └─────────────────────────────────────┘
    val glanceVersion = "1.1.0-alpha01"
    // For Glance support
    implementation("androidx.glance:glance:$glanceVersion")
    // For AppWidgets support
    implementation("androidx.glance:glance-appwidget:$glanceVersion")
    // For interop APIs with Material 3
    implementation("androidx.glance:glance-material3:$glanceVersion")


//    // dagger hilt
//    implementation("com.google.dagger:hilt-android:2.44")
//    kapt("com.google.dagger:hilt-android-compiler:2.44")

}

//kapt {
//    correctErrorTypes = true
//}
