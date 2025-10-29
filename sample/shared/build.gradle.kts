plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.jetbrains.compose.plugin)
}

kotlin {
    androidTarget()
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "Shared"
            export(projects.filePicker)
        }
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(projects.filePicker)

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.fragment)
        }
        iosMain.dependencies {
            api(projects.filePicker)
        }
    }

    jvmToolchain(8)
}

android {
    namespace = "io.github.dimaklekchyan.sample.shared"
    compileSdk = 36
    defaultConfig {
        minSdk = 23
    }
}
