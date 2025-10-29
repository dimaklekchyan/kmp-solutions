package io.github.dimaklekchyan.sample.shared

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun SampleViewController(): UIViewController {
    return ComposeUIViewController(
        content = { Sample() }
    )
}