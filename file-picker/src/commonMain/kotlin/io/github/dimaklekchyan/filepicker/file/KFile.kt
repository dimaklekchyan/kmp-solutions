package io.github.dimaklekchyan.filepicker.file

expect class KFile {
    fun name(): String
    fun extension(): String
    fun path(): String
    fun exists(): Boolean
    fun mimeType(): String
    fun getLength(): Long
}