package io.github.dimaklekchyan.filepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import io.github.dimaklekchyan.NativeImagePreviewProvider
import io.github.dimaklekchyan.filepicker.file.KFile
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.autoreleasepool
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import platform.AVFoundation.AVAssetImageGenerator
import platform.AVFoundation.AVURLAsset
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGContextDrawImage
import platform.CoreGraphics.CGContextRelease
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGImageGetWidth
import platform.CoreGraphics.CGImageRef
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMake
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSURLBookmarkCreationSuitableForBookmarkFile
import platform.Foundation.NSUUID.Companion.UUID
import platform.Foundation.NSUserDefaults
import platform.Foundation.temporaryDirectory
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIImage
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UniformTypeIdentifiers.UTTypeImage
import platform.UniformTypeIdentifiers.UTTypeMovie
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual class KFilePickerController(
    private val controller: PHPickerViewController
) {
    actual fun launch() {
        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
            controller,
            true,
            null,
        )
    }
}

@Composable
actual fun rememberSingleFilePickerController(
    type: KFilePickerType,
    persistFile: Boolean,
    stateProvider: (KFilePickerState) -> Unit,
): KFilePickerController {
    val coroutineScope = rememberCoroutineScope()
    val pickerDelegate = remember(stateProvider, coroutineScope) {
        PHPickerViewControllerDelegate(coroutineScope, stateProvider, persistFile)
    }
    val pickerController = remember(type, pickerDelegate) {
        createPHPickerViewController(
            delegate = pickerDelegate,
            type = type,
            maxItems = 1
        )
    }
    return remember(pickerController) { KFilePickerController(pickerController) }
}

@Composable
actual fun rememberMultipleFilesPickerController(
    type: KFilePickerType,
    persistFile: Boolean,
    maxItems: Int,
    stateProvider: (KFilePickerState) -> Unit,
): KFilePickerController {
    val coroutineScope = rememberCoroutineScope()
    val pickerDelegate = remember(stateProvider, coroutineScope) {
        PHPickerViewControllerDelegate(coroutineScope, stateProvider, persistFile)
    }
    val pickerController = remember(type, pickerDelegate) {
        createPHPickerViewController(
            delegate = pickerDelegate,
            type = type,
            maxItems = maxItems.toLong()
        )
    }
    return remember(pickerController) { KFilePickerController(pickerController) }
}

@OptIn(ExperimentalForeignApi::class)
private class PHPickerViewControllerDelegate(
    private val coroutineScope: CoroutineScope,
    private val stateProvider: (KFilePickerState) -> Unit,
    private val persistFile: Boolean,
): NSObject(), UINavigationControllerDelegateProtocol, PHPickerViewControllerDelegateProtocol {

    private val fileManager = NSFileManager.defaultManager
    private val imagePreviewProvider = NativeImagePreviewProvider()

    override fun picker(
        picker: PHPickerViewController,
        didFinishPicking: List<*>
    ) {
        picker.dismissViewControllerAnimated(true, null)

        coroutineScope.launch(Dispatchers.Default) {
            stateProvider(KFilePickerState.Preparing)
            try {
                val files = didFinishPicking.mapNotNull { it as? PHPickerResult }.map { result ->
                    prepareFile(result)
                }
                stateProvider(KFilePickerState.DoneMultiple(files))
            } catch (ex: Exception) {
                stateProvider(KFilePickerState.Exception(ex))
            }

        }
    }

    private suspend fun prepareFile(result: PHPickerResult): KPickedFile {
        return if (result.itemProvider.hasItemConformingToTypeIdentifier(UTTypeMovie.identifier)) {
            prepareVideoFile(result)
        } else if (result.itemProvider.hasItemConformingToTypeIdentifier(UTTypeImage.identifier)) {
            prepareImageFile(result)
        } else {
            throw IllegalArgumentException("Unknown type identifier | ${result.itemProvider.registeredTypeIdentifiers}")
        }
    }

    private suspend fun prepareVideoFile(videoResult: PHPickerResult): KPickedFile.Video {
        val fileUrl = getFileUrlByTypeIdentifier(
            result = videoResult,
            typeIdentifier = UTTypeMovie.identifier
        )
        return if (fileUrl != null) {
            val file = KFile(fileUrl)
            val info = getVideoInfo(fileUrl)
            KPickedFile.Video(file, info.preview, info.duration)
        } else {
            throw IllegalStateException("File url is null")
        }
    }

    private suspend fun prepareImageFile(imageResult: PHPickerResult): KPickedFile.Image {
        val fileUrl = getFileUrlByTypeIdentifier(
            result = imageResult,
            typeIdentifier = UTTypeImage.identifier
        )

        return if (fileUrl != null) {
            val file = KFile(fileUrl)
            val preview = getImagePreview(imageResult)
            KPickedFile.Image(file, preview)
        } else {
            throw IllegalStateException("File url is null")
        }
    }

    private suspend fun getFileUrlByTypeIdentifier(
        result: PHPickerResult,
        typeIdentifier: String
    ): NSURL? = suspendCoroutine { continuation ->
        result.itemProvider.loadFileRepresentationForTypeIdentifier(
            typeIdentifier = typeIdentifier
        ) { url, error ->
            if (error != null || url == null) {
                continuation.resume(null)
            }

            try {
                val fileName = url?.lastPathComponent ?: "temp_file"
                val targetUrl = fileManager.temporaryDirectory
                    .URLByAppendingPathComponent(fileName)

                if (targetUrl == null) {
                    continuation.resume(null)
                }

                if (fileManager.fileExistsAtPath(targetUrl!!.path ?: "")) {
                    fileManager.removeItemAtURL(targetUrl, null)
                }

                fileManager.copyItemAtURL(url!!, targetUrl, null)

                if (persistFile) targetUrl.bookmark()

                continuation.resume(targetUrl)
            } catch (e: Exception) {
                println("Failed to copy file: $e")
                continuation.resume(null)
            }
        }
    }

    private suspend fun getImagePreview(
        result: PHPickerResult,
    ): ImageBitmap? = suspendCoroutine { continuation ->
        imagePreviewProvider.provideWithItemProvider(
            itemProvider = result.itemProvider,
            onImage = { image ->
                val bitmap = (image as UIImage?)?.asImageBitmap()
                continuation.resume(bitmap)
            }
        )
    }
}

private fun createPHPickerViewController(
    delegate: PHPickerViewControllerDelegateProtocol,
    type: KFilePickerType,
    maxItems: Long
): PHPickerViewController {
    val configuration = PHPickerConfiguration().apply {
        when(type) {
            KFilePickerType.Image -> {
                filter = PHPickerFilter.imagesFilter
            }
            KFilePickerType.Video -> {
                filter = PHPickerFilter.videosFilter
            }
            KFilePickerType.All -> {}
        }
        selectionLimit = maxItems
    }
    val pickerController = PHPickerViewController(configuration = configuration)
    pickerController.delegate = delegate
    return pickerController
}

data class VideoInfo(
    val preview: ImageBitmap?,
    val duration: Int
)

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun getVideoInfo(url: NSURL): VideoInfo = autoreleasepool {
    val asset = AVURLAsset(uRL = url, options = null)
    val imageGenerator = AVAssetImageGenerator(asset).apply {
        appliesPreferredTrackTransform = true
        maximumSize = CGSizeMake(512.0, 512.0)
    }

    val cgImage = imageGenerator.copyCGImageAtTime(
        requestedTime = CMTimeMake(value = 0, timescale = 1),
        actualTime = null,
        error = null
    )

    val preview = cgImage?.asImageBitmap()

    val duration: Int = CMTimeGetSeconds(asset.duration).toInt()

    VideoInfo(preview, duration)
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun UIImage.asImageBitmap(maxSize: Double = 512.0): ImageBitmap? {
    return autoreleasepool {
        val originalSize = this.size
        val originalWidth = originalSize.useContents { width }
        val originalHeight = originalSize.useContents { height }
        val scaleFactor = minOf(maxSize / originalWidth, maxSize / originalHeight, 1.0)
        val targetSize = CGSizeMake(
            width = originalWidth * scaleFactor,
            height = originalHeight * scaleFactor
        )
        val targetWidth = targetSize.useContents { width }
        val targetHeight = targetSize.useContents { height }

        val resized = UIGraphicsImageRenderer(targetSize).imageWithActions { _ ->
            this.drawInRect(CGRectMake(0.0, 0.0, targetWidth, targetHeight))
        }

        resized.CGImage?.asImageBitmap()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun CGImageRef.asImageBitmap(): ImageBitmap {
    val width = CGImageGetWidth(this).toInt()
    val height = CGImageGetHeight(this).toInt()
    val colorSpace = CGColorSpaceCreateDeviceRGB()
    val bytesPerRow = width * 4
    val buffer = ByteArray(bytesPerRow * height)

    memScoped {
        val ctx = CGBitmapContextCreate(
            data = buffer.refTo(0).getPointer(this),
            width = width.toULong(),
            height = height.toULong(),
            bitsPerComponent = 8u,
            bytesPerRow = bytesPerRow.toULong(),
            space = colorSpace,
            bitmapInfo = CGImageAlphaInfo.kCGImageAlphaPremultipliedFirst.value or 0x2000u
        )
        CGContextDrawImage(ctx, CGRectMake(0.0, 0.0, width.toDouble(), height.toDouble()), this@asImageBitmap)
        CGContextRelease(ctx)
    }

    val skImage = Image.makeRaster(
        ImageInfo.makeS32(width, height, ColorAlphaType.PREMUL),
        buffer,
        bytesPerRow
    )

    return skImage.toComposeImageBitmap()
}

@OptIn(ExperimentalForeignApi::class)
private fun NSURL.bookmark(store: NSUserDefaults = NSUserDefaults.standardUserDefaults) {
    memScoped {
        val bookmarkData: NSData? = this@bookmark.bookmarkDataWithOptions(
            options = NSURLBookmarkCreationSuitableForBookmarkFile,
            includingResourceValuesForKeys = null,
            relativeToURL = null,
            error = null
        )

        if (bookmarkData != null) {
            val key = this@bookmark.absoluteString ?: UUID().UUIDString()
            store.setObject(bookmarkData, key)
        }
    }
}
