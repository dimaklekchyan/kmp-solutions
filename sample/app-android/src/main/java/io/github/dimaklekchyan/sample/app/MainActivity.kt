package io.github.dimaklekchyan.sample.app

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import io.github.dimaklekchyan.sample.shared.setupSample

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupSample()
    }
}