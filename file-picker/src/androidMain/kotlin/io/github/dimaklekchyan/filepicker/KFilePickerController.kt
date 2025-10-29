package io.github.dimaklekchyan.filepicker

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.CancellationSignal
import android.provider.MediaStore
import android.util.Size
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import io.github.dimaklekchyan.filepicker.file.KFile
import java.io.File

actual class KFilePickerController(
    private val type: KFilePickerType,
    private val launcher: ManagedActivityResultLauncher<PickVisualMediaRequest, *>
) {
    actual fun launch() {
        launcher.launch(type.toPickVisualMediaRequest())
    }
}

@Composable
actual fun rememberSingleFilePickerController(
    type: KFilePickerType,
    persistFile: Boolean,
    stateProvider: (KFilePickerState) -> Unit,
): KFilePickerController {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                stateProvider(KFilePickerState.Preparing)
                try {
                    val pickedFile: KPickedFile = context.prepareFile(uri, type, persistFile)
                    stateProvider(KFilePickerState.Done(pickedFile))
                } catch (e: Exception) {
                    stateProvider(KFilePickerState.Exception(e))
                }
            }
        },
    )

    return remember(type, launcher) { KFilePickerController(type, launcher) }
}

@Composable
actual fun rememberMultipleFilesPickerController(
    type: KFilePickerType,
    persistFile: Boolean,
    maxItems: Int,
    stateProvider: (KFilePickerState) -> Unit,
): KFilePickerController {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems),
        onResult = { uriList ->
            stateProvider(KFilePickerState.Preparing)
            try {
                val files = uriList.map { uri -> context.prepareFile(uri, type, persistFile) }
                stateProvider(KFilePickerState.DoneMultiple(files))
            } catch (e: Exception) {
                stateProvider(KFilePickerState.Exception(e))
            }
        },
    )

    return remember(type, launcher) { KFilePickerController(type, launcher) }
}

private fun Context.prepareFile(
    uri: Uri,
    type: KFilePickerType,
    persistFile: Boolean
): KPickedFile {
    if (persistFile) takePersistableUriPermission(uri)
    val file = getFileByUri(uri)
    val mimeType = file.mimeType()
    return when {
        type == KFilePickerType.Image || mimeType.contains("image") -> {
            val preview = getImagePreview(uri)
            KPickedFile.Image(file, preview)
        }
        type == KFilePickerType.Video || mimeType.contains("video") -> {
            val info = getVideoInfo(uri)
            KPickedFile.Video(file, info?.preview, info?.duration ?: -1)
        }
        else -> {
            throw IllegalArgumentException("Unknown mimeType $mimeType")
        }
    }
}

private fun KFilePickerType.toPickVisualMediaRequest(): PickVisualMediaRequest {
    return when (this) {
        KFilePickerType.Image -> PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        KFilePickerType.Video -> PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
        else -> PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
    }
}

private fun Context.getFileByUri(uri: Uri): KFile {
    return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
        val filePath = getFilePathByUri(uri)
        if (filePath != null) {
            KFile(filePath)
        } else {
            getTempFileByUri(uri)
        }
    } else {
        getTempFileByUri(uri)
    }
}

private fun Context.getFilePathByUri(uri: Uri): String? {
    val projections = arrayOf(MediaStore.Files.FileColumns.DATA)
    return contentResolver
        .query(uri, projections, null, null, null)
        ?.use { cursor ->
            cursor.moveToFirst()
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val filePath = cursor.getString(columnIndex)
            filePath
        }
}

private fun Context.getTempFileByUri(uri: Uri): KFile {
    val extension = contentResolver.getType(uri)?.substringAfter("/")
    val name: String = contentResolver
        .query(uri, arrayOf(MediaStore.Files.FileColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor ->
            cursor.moveToFirst()
            val columnIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
            if (columnIndex != -1) {
                cursor.getString(columnIndex)
            } else {
                null
            }
        } ?: "tempFile"

    val file = File.createTempFile(name.substringBefore("."), ".$extension")

    contentResolver.openInputStream(uri).use { input ->
        file.outputStream().use { output ->
            input?.copyTo(output)
        }
    }

    return KFile(file)
}

private fun Context.getImagePreview(uri: Uri): ImageBitmap? {
    val thumbnail: Bitmap? = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
        contentResolver.loadThumbnail(uri, Size(15000, 15000), CancellationSignal())
    } else {
        contentResolver.query(uri, arrayOf(MediaStore.Images.ImageColumns._ID), null, null, null)
            ?.use { cursor ->
                cursor.moveToNext()
                val idColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID)
                val id = cursor.getLong(idColumnIndex)
                MediaStore.Images.Thumbnails
                    .getThumbnail(contentResolver, id, MediaStore.Images.Thumbnails.MINI_KIND, null)
            }
    }
    return thumbnail?.asImageBitmap()
}

private data class VideoInfo(
    val preview: ImageBitmap,
    val duration: Int
)

private fun Context.getVideoInfo(uri: Uri): VideoInfo? {
    return contentResolver
        .query(uri, arrayOf(MediaStore.Video.VideoColumns._ID, MediaStore.Video.Media.DURATION), null, null, null)
        ?.use { cursor ->
            cursor.moveToNext()
            val idColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns._ID)
            val durationColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)

            val id = cursor.getLong(idColumnIndex)
            val duration = cursor.getInt(durationColumnIndex) / 1000

            val mediaStoreThumbnail = MediaStore.Video.Thumbnails
                .getThumbnail(contentResolver, id, MediaStore.Video.Thumbnails.MINI_KIND, null)

            val thumbnail: Bitmap? = mediaStoreThumbnail
                ?: contentResolver.openFileDescriptor(uri, "r")?.use {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(it.fileDescriptor)
                    retriever.getFrameAtTime(0)
                }

            thumbnail?.asImageBitmap()?.let {
                VideoInfo(it, duration)
            }
        }
}

private fun Context.takePersistableUriPermission(uri: Uri) {
    try {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    } catch (e: Exception) {
        println("FilePicker | ERROR: $e | ${e.cause} | ${e.message}")
    }
}