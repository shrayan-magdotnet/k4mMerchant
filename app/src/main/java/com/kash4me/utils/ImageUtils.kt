package com.kash4me.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File


class ImageUtils {

    companion object {
        const val NO_IMAGE_AVAILABLE =
            "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"
    }

    suspend fun decodeImageFromBase64(context: Context, base64String: String?): Bitmap {

        return withContext(Dispatchers.IO) {
            if (base64String == null) {
                Timber.d("Base64 string was null")
                return@withContext getNoImageAvailableBitmap(context)
            }

            val decodedString: ByteArray = Base64.decode(base64String, Base64.DEFAULT)
            val decodedImage = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

            if (decodedImage == null) {
                Timber.d("Couldn't decode anything from the provided base64 string")
                return@withContext getNoImageAvailableBitmap(context)
            }

            Timber.d("Returning the decoded image from provided base64 string")
            return@withContext decodedImage
        }

    }

    suspend fun getNoImageAvailableBitmap(context: Context): Bitmap {
        return decodeImageFromBase64(context = context, base64String = NO_IMAGE_AVAILABLE)
    }

    fun getNameInitialsImage(fullName: String): TextDrawable? {
        return TextDrawable.builder()
            .beginConfig()
            ?.textColor(Color.GRAY)
            ?.bold()
            ?.endConfig()
            ?.buildRect(getNameInitials(fullName))
    }

    // Function to convert image from File to Base64
    fun imageFileToBase64(file: File): String? {
        try {
            // Step 1: Load the image from File into a Bitmap object
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)

            // Step 2: Convert the Bitmap to a byte array
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()

            // Step 3: Encode the byte array to Base64
            return Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

}