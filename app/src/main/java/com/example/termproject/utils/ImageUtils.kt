package com.example.termproject.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ImageUtils {
    /**
     * Copies an image from the provided URI to the app's internal storage
     * and returns the local file object.
     */
    fun saveImageToInternalStorage(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null

            // Create a file in internal storage
            val file = File(context.filesDir, "saved_image_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)

            // Copy data
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            file // Return the local file for your app to use
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}