package io.github.dimaklekchyan.filepicker

import androidx.compose.runtime.Composable

/**
 * Creates a [KFilePickerController] to pick single image
 * @param persistFile bookmark file on iOS and take persistable uri on Android if true.
 * @param onDone the callback to be called when image picked
 * @param onPreparing the callback to be called when preparing started
 * @param onException the callback to be called when exception occurred
 * @return the controller that can be used to launch the native picker.
 * */
@Composable
fun rememberSingleImagePickerController(
    persistFile: Boolean = true,
    onDone: (KPickedFile.Image) -> Unit,
    onPreparing: () -> Unit = {},
    onException: (Exception) -> Unit = {}
): KFilePickerController {
    return rememberSingleFilePickerController(
        type = KFilePickerType.Image,
        persistFile = persistFile,
        stateProvider = { state ->
            when (state) {
                is KFilePickerState.Preparing -> onPreparing()
                is KFilePickerState.Done -> onDone(state.file as KPickedFile.Image)
                is KFilePickerState.Exception -> onException(state.error)
                is KFilePickerState.DoneMultiple -> {
                    if (state.files.size == 1) {
                        onDone(state.files.first() as KPickedFile.Image)
                    }
                }
            }
        }
    )
}

/**
 * Creates a [KFilePickerController] to pick multiple images
 * @param persistFile bookmark file on iOS and take persistable uri on Android if true.
 * @param onDone the callback to be called when images picked
 * @param onPreparing the callback to be called when preparing started
 * @param onException the callback to be called when exception occurred
 * @return the controller that can be used to launch the native picker.
 * */
@Composable
fun rememberMultipleImagesPickerController(
    persistFile: Boolean = true,
    maxItems: Int = Int.MAX_VALUE,
    onDone: (List<KPickedFile.Image>) -> Unit,
    onPreparing: () -> Unit = {},
    onException: (Exception) -> Unit = {}
): KFilePickerController {
    return rememberMultipleFilesPickerController(
        type = KFilePickerType.Image,
        persistFile = persistFile,
        maxItems = maxItems,
        stateProvider = { state ->
            when (state) {
                is KFilePickerState.Preparing -> onPreparing()
                is KFilePickerState.Done -> {}
                is KFilePickerState.Exception -> onException(state.error)
                is KFilePickerState.DoneMultiple -> onDone(state.files.map { it as KPickedFile.Image })
            }
        }
    )
}