package io.github.dimaklekchyan.sample.shared.filePicker

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.github.dimaklekchyan.filepicker.KPickedFile
import io.github.dimaklekchyan.filepicker.rememberMultipleImagesPickerController
import io.github.dimaklekchyan.filepicker.rememberMultipleVideosPickerController
import io.github.dimaklekchyan.filepicker.rememberSingleImagePickerController
import io.github.dimaklekchyan.filepicker.rememberSingleVideoPickerController

@Composable
internal fun FilePickerSample(
    onBackClick: () -> Unit
) {
    var files by remember { mutableStateOf<List<KPickedFile>>(emptyList()) }

    val singleImagePiker = rememberSingleImagePickerController(
        onDone = { files = listOf(it) },
    )
    val multipleImagePiker = rememberMultipleImagesPickerController(
        onDone = { files = it },
    )
    val singleVideoPiker = rememberSingleVideoPickerController(
        onDone = { files = listOf(it) },
    )
    val multipleVideosPiker = rememberMultipleVideosPickerController(
        onDone = { files = it },
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
        Button(
            onClick = { singleImagePiker.launch() }
        ) { Text(text = "Launch single image picker") }
        Button(
            onClick = { multipleImagePiker.launch() }
        ) { Text(text = "Launch multiple images picker") }
        Button(
            onClick = { singleVideoPiker.launch() }
        ) { Text(text = "Launch single video picker") }
        Button(
            onClick = { multipleVideosPiker.launch() }
        ) { Text(text = "Launch multiple videos picker") }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            items(
                items = files,
            ) { file ->
                file.preview?.let {
                    Image(
                        bitmap = it,
                        modifier = Modifier.size(100.dp),
                        contentScale = ContentScale.FillWidth,
                        contentDescription = null
                    )
                }
            }
        }
    }
}