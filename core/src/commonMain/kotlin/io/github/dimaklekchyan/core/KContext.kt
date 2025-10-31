package io.github.dimaklekchyan.core

import androidx.compose.runtime.Composable

expect class KContext

@Composable
expect fun rememberKContext(): KContext