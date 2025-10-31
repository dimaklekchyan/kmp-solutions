Core is a library with useful utils

## Overview
Here are some key concepts of the library.

* [KContext](../core/src/commonMain/kotlin/io/github/dimaklekchyan/core/KContext.kt) - a wrapper over context on Android and blank class on iOS
* [KFile](../core/src/commonMain/kotlin/io/github/dimaklekchyan/core/KFile.kt) - a wrapper over native file

## Installation

project **build.gradle.kts**
```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.dimaklekchyan:core:0.1.0")
        }
    }
}
```