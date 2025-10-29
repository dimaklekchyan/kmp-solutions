@file:OptIn(ExperimentalForeignApi::class)

package io.github.dimaklekchyan.filepicker.file

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

val NSURL.exists: Boolean
    get() {
        return memScoped {
            val isDirectory = alloc<BooleanVar>()
            NSFileManager.defaultManager.fileExistsAtPath(path!!, isDirectory.ptr)
        }
    }
