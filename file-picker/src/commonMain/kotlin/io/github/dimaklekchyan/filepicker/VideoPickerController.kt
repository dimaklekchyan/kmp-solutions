package io.github.dimaklekchyan.filepicker

import androidx.compose.runtime.Composable

/**
 * Creates a [KFilePickerController] to pick single video
 * @param persistFile bookmark file on iOS and take persistable uri on Android if true.
 * @param onDone the callback to be called when video picked
 * @param onPreparing the callback to be called when preparing started
 * @param onException the callback to be called when exception occurred
 * @return the controller that can be used to launch the native picker.
 * */
@Composable
fun rememberSingleVideoPickerController(
    persistFile: Boolean = true,
    onDone: (KPickedFile.Video) -> Unit,
    onPreparing: () -> Unit = {},
    onException: (Exception) -> Unit = {}
): KFilePickerController {
    return rememberSingleFilePickerController(
        type = KFilePickerType.Video,
        persistFile = persistFile,
        stateProvider = { state ->
            when (state) {
                is KFilePickerState.Preparing -> onPreparing()
                is KFilePickerState.Done -> onDone(state.file as KPickedFile.Video)
                is KFilePickerState.Exception -> onException(state.error)
                is KFilePickerState.DoneMultiple -> {
                    if (state.files.size == 1) {
                        onDone(state.files.first() as KPickedFile.Video)
                    }
                }
            }
        }
    )
}

/**
 * Creates a [KFilePickerController] to pick multiple videos
 * @param persistFile bookmark file on iOS and take persistable uri on Android if true.
 * @param onDone the callback to be called when videos picked
 * @param onPreparing the callback to be called when preparing started
 * @param onException the callback to be called when exception occurred
 * @return the controller that can be used to launch the native picker.
 * */
@Composable
fun rememberMultipleVideosPickerController(
    persistFile: Boolean = true,
    maxItems: Int = Int.MAX_VALUE,
    onDone: (List<KPickedFile.Video>) -> Unit,
    onPreparing: () -> Unit = {},
    onException: (Exception) -> Unit = {}
): KFilePickerController {
    return rememberMultipleFilesPickerController(
        type = KFilePickerType.Video,
        persistFile = persistFile,
        maxItems = maxItems,
        stateProvider = { state ->
            when (state) {
                is KFilePickerState.Preparing -> onPreparing()
                is KFilePickerState.Done -> {}
                is KFilePickerState.Exception -> onException(state.error)
                is KFilePickerState.DoneMultiple -> onDone(state.files.map { it as KPickedFile.Video })
            }
        }
    )
}