plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.jetbrains.kotlin.compose) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.maps.secrets) apply false
//    id("com.google.dagger.hilt.android") version "2.44" apply false
}