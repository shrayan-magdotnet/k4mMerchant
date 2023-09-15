package com.kash4me.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.MimeTypeMap
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class FileUtils {

    fun createFileFromUri(context: Context, uri: Uri): File? {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = createTempFile(context, uri)

        Timber.d("Temporary file -> $tempFile")

        if (inputStream != null) {
            try {
                val outputStream = FileOutputStream(tempFile)
                inputStream.copyTo(outputStream)
                outputStream.close()
                inputStream.close()
                return tempFile
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return null
    }

    fun createTempFile(context: Context, uri: Uri): File {
        val tempFileName = "temp_file"
        val tempDir = context.cacheDir

        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri)

        val fileExtension = if (mimeType != null) {
            MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        } else {
            // If the mimeType is null, attempt to extract the extension from the Uri
            MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        }

        val extension = if (fileExtension != null) {
            ".$fileExtension"
        } else {
            ".tmp" // Default extension if unable to determine
        }

        Timber.d("File extension -> $fileExtension")

        return File.createTempFile(tempFileName, extension, tempDir)
    }

    fun getFileSizeInMB(file: File): Double {
        val fileSizeBytes = file.length()
        val fileSizeKB = fileSizeBytes / 1024.0
        return fileSizeKB / 1024.0 // Size in Megabytes
    }

    // Save the bitmap as an image file
    fun saveBitmapAsFile(context: Context, bitmap: Bitmap): File? {
        // Get the directory for storing the image file
        val directory = context.filesDir
        // Create a unique file name
        val fileName = "image_${System.currentTimeMillis()}.jpg"
        // Create the file object
        val file = File(directory, fileName)

        return try {
            // Create a file output stream
            val outputStream = FileOutputStream(file)
            // Compress the bitmap to JPEG format with 100% quality (optional)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, outputStream)
            // Flush and close the output stream
            outputStream.flush()
            outputStream.close()
            file
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

}