package io.github.dimaklekchyan.filepicker

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import io.github.dimaklekchyan.core.KFile

/**
 * A controller to launch native file picker.
 * It uses LauncherForActivityResult with PickVisualMedia contract on Android and PHPickerViewController on iOS
 * */
expect class KFilePickerController {
    fun launch()
}

/**
 * Creates a [KFilePickerController] to pick single file
 * @param type the type that determines the selection of files. See [KFilePickerType]
 * @param persistFile bookmark file on iOS and take persistable uri on Android if true. False by default.
 * @param stateProvider the callback to be called when file picker has any state. See [KFilePickerState]
 * @return the controller that can be used to launch the native picker.
 * */
@Composable
expect fun rememberSingleFilePickerController(
    type: KFilePickerType = KFilePickerType.All,
    persistFile: Boolean = false,
    stateProvider: (KFilePickerState) -> Unit,
): KFilePickerController

/**
 * Creates a [KFilePickerController] to pick multiple files
 * @param type the type that determines the selection of files. See [KFilePickerType]
 * @param maxItems the max amount of picked files. [Int.MAX_VALUE] by default.
 * @param persistFile bookmark file on iOS and take persistable uri on Android if true. False by default.
 * @param stateProvider the callback to be called when file picker has any state. See [KFilePickerState]
 * @return the controller that can be used to launch the native picker.
 * */
@Composable
expect fun rememberMultipleFilesPickerController(
    type: KFilePickerType = KFilePickerType.All,
    persistFile: Boolean = false,
    maxItems: Int = Int.MAX_VALUE,
    stateProvider: (KFilePickerState) -> Unit,
): KFilePickerController

/** A state that represent a result or progress of file picking */
sealed class KFilePickerState {
    /** Sent when files picking is made */
    object Preparing: KFilePickerState()
    /** Sent after preparing of single file is completed */
    class Done(var file: KPickedFile): KFilePickerState()
    /** Sent after preparing of multiple files is completed */
    class DoneMultiple(var files: List<KPickedFile>): KFilePickerState()
    /** Sent when an exception is thrown */
    class Exception(val error: kotlin.Exception): KFilePickerState()
}

/**
 * A file picked by controller
 * @param file the file
 * @param preview the optional ImageBitmap to represent a file
 * */
sealed class KPickedFile(
    val file: KFile,
    val preview: ImageBitmap?
) {
    class Image(
        file: KFile,
        preview: ImageBitmap?
    ): KPickedFile(file, preview)

    class Video(
        file: KFile,
        preview: ImageBitmap?,
        val durationSec: Int
    ): KPickedFile(file, preview)
}

enum class KFilePickerType(val value: String) {
    Image("image/*"),
    Video("video/*"),
    All("*/*"),
}