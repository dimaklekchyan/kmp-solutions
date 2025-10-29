package io.github.dimaklekchyan.filepicker.file

import android.os.Build
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files

actual class KFile(val file: File) {

    constructor(path: String) : this(File(path))
    constructor(parent: String, child: String) : this(File(parent, child))

    actual fun name(): String = file.nameWithoutExtension
    actual fun extension(): String = file.extension
    actual fun path(): String = file.path
    actual fun exists(): Boolean = file.exists()
    actual fun mimeType(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Files.probeContentType(file.toPath())
        } else {
            MimeTypeMap.getFileExtensionFromUrl(path())?.let {
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension())
            } ?: "application/octet-stream"
        }
    }
    actual fun getLength(): Long = file.length()
}