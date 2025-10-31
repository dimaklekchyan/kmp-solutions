package io.github.dimaklekchyan.sample.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.dimaklekchyan.sample.shared.filePicker.FilePickerSample
import io.github.dimaklekchyan.sample.shared.sensor.SensorSample

private enum class SampleType { FilePicker, Sensor, None }
@Composable
internal fun Sample() {
    var type by remember { mutableStateOf(SampleType.None) }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when(type) {
            SampleType.FilePicker -> {
                FilePickerSample { type = SampleType.None }
            }
            SampleType.Sensor -> {
                SensorSample { type = SampleType.None }
            }
            SampleType.None -> {
                Button(
                    onClick = { type = SampleType.FilePicker }
                ) { Text(text = "Go to FilePicker sample") }
                Button(
                    onClick = { type = SampleType.Sensor }
                ) { Text(text = "Go to Sensor sample") }
            }
        }
    }
}