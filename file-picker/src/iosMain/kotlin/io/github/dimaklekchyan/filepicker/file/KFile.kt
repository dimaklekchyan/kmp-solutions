package io.github.dimaklekchyan.filepicker.file

import platform.Foundation.NSFileHandle
import platform.Foundation.NSURL
import platform.Foundation.fileHandleForReadingAtPath
import platform.Foundation.readDataToEndOfFile
import platform.UniformTypeIdentifiers.UTType

actual class KFile(val nsurl: NSURL) {
    actual fun name(): String = nsurl.lastPathComponent?.substringBeforeLast(".") ?: ""
    actual fun extension(): String = nsurl.pathExtension ?: ""
    actual fun path(): String = nsurl.path ?: ""
    actual fun exists(): Boolean = nsurl.exists
    actual fun mimeType(): String {
        val ext = nsurl.pathExtension
        return ext?.let {
            UTType.Companion.typeWithFilenameExtension(ext)?.preferredMIMEType
        } ?: "application/octet-stream"
    }
    actual fun getLength(): Long {
        val fileHandle = NSFileHandle.Companion.fileHandleForReadingAtPath(path = nsurl.path!!)  ?: throw Exception("NSFileHandle is null")
        val data = fileHandle.readDataToEndOfFile()
        return data.length.toLong()
    }
}