package io.github.dimaklekchyan.sample.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.layout.ContentScale
import io.github.dimaklekchyan.filepicker.KPickedFile
import io.github.dimaklekchyan.filepicker.rememberSingleImagePickerController

@Composable
internal fun Sample() {
    var image by remember { mutableStateOf<KPickedFile.Image?>(null) }
    val controller = rememberSingleImagePickerController(
        onDone = { image = it },
    )

    Column(
        modifier = Modifier.fillMaxSize().systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { controller.launch() }
        ) { Text(text = "Launch picker") }

        image?.preview?.let {
            Image(
                bitmap = it,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth,
                contentDescription = null
            )
        }
    }
}