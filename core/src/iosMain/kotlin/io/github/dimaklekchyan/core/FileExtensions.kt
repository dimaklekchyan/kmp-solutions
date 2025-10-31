@file:OptIn(ExperimentalForeignApi::class)

package io.github.dimaklekchyan.core

import kotlinx.cinterop.BooleanVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.value
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileHandle
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.fileHandleForReadingFromURL
import platform.Foundation.readDataToEndOfFile
import platform.Foundation.stringWithContentsOfURL
import platform.Foundation.writeToURL
@OptIn(ExperimentalForeignApi::class)
val NSFileManager.DocumentDirectory
    get() = URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        create = true,
        appropriateForURL = null,
        error = null
    )!!

@Suppress("FunctionName")
fun File(dir: NSURL, child: String): NSURL = dir.URLByAppendingPathComponent(child)!!

@Suppress("FunctionName")
fun File(dir: String, child: String): NSURL = NSURL(fileURLWithPath = "$dir/$child")

val NSURL.isDirectory: Boolean
    get() {
        return memScoped {
            val isDirectory = alloc<BooleanVar>()
            val fileExists = NSFileManager.defaultManager.fileExistsAtPath(path!!, isDirectory.ptr)
            fileExists && isDirectory.value
        }
    }

val NSURL.exists: Boolean
    get() {
        return memScoped {
            val isDirectory = alloc<BooleanVar>()
            NSFileManager.defaultManager.fileExistsAtPath(path!!, isDirectory.ptr)
        }
    }

@OptIn(ExperimentalForeignApi::class)
fun NSURL.mkdirs(): Boolean {
    return NSFileManager.defaultManager.createDirectoryAtURL(this, true, null, null)
}

@OptIn(ExperimentalForeignApi::class)
fun NSURL.createFile(): Boolean {
    return NSFileManager.defaultManager.createFileAtPath(path = this.path!!, null, null)
}

@OptIn(ExperimentalForeignApi::class)
fun NSURL.listFiles(filter: (NSURL, String) -> Boolean) =
    NSFileManager.defaultManager.contentsOfDirectoryAtPath(path!!, null)
        ?.map { it.toString() }
        ?.filter { filter(this, it) }
        ?.map { File(this, it) }
        ?.toTypedArray()

@OptIn(ExperimentalForeignApi::class)
fun NSURL.delete(): Boolean {
    return NSFileManager.defaultManager.removeItemAtURL(this, null)
}

fun NSURL.readBytesWithFileManager(): ByteArray {
    val manager = NSFileManager.defaultManager
    val data: NSData = manager.contentsAtPath(this.path ?: "") ?: throw Exception("NSData is null")
    val nativeBytes = data.bytes ?: throw Exception("NSData.bytes is null")
    return nativeBytes.readBytes(data.length.toInt())
}

@OptIn(ExperimentalForeignApi::class)
fun NSURL.readBytesWithFileHandle(): ByteArray {
    val isSecretFile = this.startAccessingSecurityScopedResource()

    val fileHandle: NSFileHandle = NSFileHandle.fileHandleForReadingFromURL(this, null)  ?: throw Exception("NSFileHandle is null")
    val data = fileHandle.readDataToEndOfFile()
    val nativeBytes = data.bytes ?: throw Exception("NSData.bytes is null")
    val byteArray = nativeBytes.readBytes(data.length.toInt())
    if(isSecretFile) this.stopAccessingSecurityScopedResource()

    return byteArray
}

@OptIn(ExperimentalForeignApi::class)
fun NSURL.readText(): String =
    NSString.stringWithContentsOfURL(
        url = this,
        encoding = NSUTF8StringEncoding,
        error = null,
    ) as String

@OptIn(ExperimentalForeignApi::class)
fun NSURL.writeText(text: String) {
    (text as NSString).writeToURL(
        url = this,
        atomically = true,
        encoding = NSUTF8StringEncoding,
        error = null
    )
}