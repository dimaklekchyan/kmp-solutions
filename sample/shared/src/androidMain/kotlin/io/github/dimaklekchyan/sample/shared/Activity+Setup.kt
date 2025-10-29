package io.github.dimaklekchyan.sample.shared

import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity

fun FragmentActivity.setupSample() {
    setContent { Sample() }
}