package com.bloomhaven.app.core.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

/**
 * Copies a picked gallery image into Bloom Haven's private app storage so memories don't
 * silently break when the source Gallery item is moved or deleted (spec section 5.6).
 * Returns the absolute path of the private copy, or null if the copy failed.
 */
object PhotoStorage {
    private const val DIR_NAME = "bloom_photos"

    fun copyIntoPrivateStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val dir = File(context.filesDir, DIR_NAME).apply { mkdirs() }
            val destFile = File(dir, "${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                destFile.outputStream().use { output -> input.copyTo(output) }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}
