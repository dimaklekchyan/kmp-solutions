plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.compose.plugin)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "io.github.dimaklekchyan.sample.app"
    compileSdk = 36
    defaultConfig {
        applicationId = "io.github.dimaklekchyan.sample.app"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(projects.sample.shared)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.fragment)
}