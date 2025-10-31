plugins {
    alias(libs.plugins.jetbrains.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    `maven-publish`
    alias(libs.plugins.vanniktechMavenPublish)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.jetbrains.compose.plugin)
}

group = "io.github.dimaklekchyan"
version = "0.1.0"

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        withSourcesJar(publish = true)
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "KSensor"
        }
    }
    jvmToolchain(17)

    sourceSets {
        commonMain.dependencies {
            api(projects.core)
            implementation(compose.foundation)
            implementation(compose.runtime)
        }
    }
}

android {
    namespace = "io.github.dimaklekchyan.sensor"
    compileSdk = 36
    defaultConfig {
        minSdk = 23
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

publishing.publications
    .withType<MavenPublication>()
    .configureEach {
        groupId = project.group.toString()
        version = project.version.toString()

        pom {
            name = "KSensor"
            url = "https://github.com/dimaklekchyan/kmp-solutions"
            description = "It is a kotlin multiplatform library for obtaining information from device sensors."

            issueManagement {
                system = "GitHub"
                url = "https://github.com/dimaklekchyan/kmp-solutions/issues"
            }

            licenses {
                license {
                    name = "The Apache Software License, Version 2.0"
                    url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                }
            }

            scm {
                connection = "scm:git:git://github.com/dimaklekchyan/kmp-solutions.git"
                developerConnection = "scm:git:ssh://github.com/dimaklekchyan/kmp-solutions.git"
                url = "https://github.com/dimaklekchyan/kmp-solutions"
            }

            developers {
                developer {
                    id = "dimaklekchyan"
                    name = "Dima Klekchyan"
                    email = "dima.klekchyan@gmail.com"
                }
            }
        }
    }

publishing {
    publications {
        repositories {
            mavenLocal()

            maven(url = uri(rootProject.layout.buildDirectory.file("maven-repo"))) {
                name = "BuildDir"
            }
        }
    }
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = false)
    signAllPublications()
}