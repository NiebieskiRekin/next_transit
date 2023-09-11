pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {


    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io")}
        maven {url = uri("https://plugins.gradle.org/m2/") }

    }
}

rootProject.name = "Next Transit"
include(":app")
