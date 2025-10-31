package io.github.dimaklekchyan.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

actual class KContext

@Composable
actual fun rememberKContext(): KContext = remember { KContext() }