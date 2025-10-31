package io.github.dimaklekchyan.sample.shared.sensor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import io.github.dimaklekchyan.core.rememberKContext
import io.github.dimaklekchyan.sensor.KOrientationSensor

@Composable
internal fun SensorSample(
    onBackClick: () -> Unit
) {
    val kContext = rememberKContext()

    val orientationSensor = remember(kContext) {
        KOrientationSensor(kContext)
    }

    val orientation by orientationSensor.eulerAnglesFlow.collectAsState()

    DisposableEffect(Unit) {
        orientationSensor.registerListener()
        onDispose { orientationSensor.unregisterListener() }
    }

    LifecycleEventEffect(
        event = Lifecycle.Event.ON_RESUME,
        onEvent = {
            orientationSensor.registerListener()
        }
    )
    LifecycleEventEffect(
        event = Lifecycle.Event.ON_PAUSE,
        onEvent = {
            orientationSensor.unregisterListener()
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        IconButton(
            modifier = Modifier.align(Alignment.Start),
            onClick = onBackClick,
            content = {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "back arrow",
                    tint = Color.Black
                )
            }
        )

        Text(
            text = "Orientation | Roll: ${orientation.roll.toInt()} | Pitch: ${orientation.pitch.toInt()} | Yaw: ${orientation.yaw.toInt()}"
        )
    }
}