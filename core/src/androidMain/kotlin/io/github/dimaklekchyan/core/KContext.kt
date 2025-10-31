package io.github.dimaklekchyan.core

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

actual class KContext(val context: Context)

@Composable
actual fun rememberKContext(): KContext {
    val context = LocalContext.current
    return remember(context) { KContext(context) }
}