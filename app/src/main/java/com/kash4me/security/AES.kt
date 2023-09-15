package com.kash4me.security

import android.content.Context
import android.util.Base64
import com.kash4me.utils.AppConstants
import timber.log.Timber
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class AES {

    lateinit var ivValue: ByteArray

    fun encrypt(context: Context, strToEncrypt: String): ByteArray {
        val plainText = strToEncrypt.toByteArray(Charsets.UTF_8)
        val key = generateKey(AppConstants.keyValue)
        val cipher = Cipher.getInstance("AES/CFB/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val cipherText = cipher.doFinal(plainText)
        ivValue = cipher.iv
        return cipherText
    }

    fun decrypt(context: Context, dataToDecrypt: ByteArray): String {
        val cipher = Cipher.getInstance("AES/CFB/NoPadding")
        val key = generateKey(AppConstants.keyValue)
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(ivValue))
        val cipherText = cipher.doFinal(dataToDecrypt)
        return buildString(cipherText, "decrypt")
    }

    private fun generateKey(password: String): SecretKeySpec {
        val digest: MessageDigest = MessageDigest.getInstance("SHA-256")
        val bytes = password.toByteArray()
        digest.update(bytes, 0, bytes.size)
        val key = digest.digest()
        val secretKeySpec = SecretKeySpec(key, "AES")
        return secretKeySpec
    }

    private fun buildString(text: ByteArray, status: String): String {
        val sb = StringBuilder()
        for (char in text) {
            sb.append(char.toInt().toChar())
        }
        Timber.d("Original word -> $sb")
        return sb.toString()
    }

    @Throws(Exception::class)
    fun decryptString(encryptedString: String, key: ByteArray): String {
        // Decode the base64 encoded string
        val decodedMessage = Base64.decode(encryptedString, Base64.DEFAULT)

        Timber.d("Decoded message -> ${decodedMessage.contentToString()}")

        // Get the IV from the decoded message
        val iv = IvParameterSpec(decodedMessage.copyOfRange(0, 16))

        Timber.d("IV -> ${iv.iv.contentToString()}")

        // Get the encrypted data from the decoded message
        val encryptedData = decodedMessage.copyOfRange(16, decodedMessage.size)

        Timber.d("Encrypted message -> ${encryptedData.decodeToString()}")

        // Create the AES cipher object with CBC mode
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        val secretKeySpec = SecretKeySpec(key, "AES")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv)

        // Decrypt the data
        val decryptedData = cipher.doFinal(encryptedData)

        Timber.d("Decrypted data -> ${decryptedData.decodeToString()}")

        // Remove the padding from the decrypted data
        val unpaddedData = decryptedData.copyOfRange(0, decryptedData.size)

        Timber.d("After removing padding -> ${unpaddedData.decodeToString()}")

        // Convert the decrypted data to a string
        val decryptedString = String(unpaddedData)

        Timber.d("Final decryption data (what we need) -> $decryptedString")

        return decryptedString
    }

    @Throws(Exception::class)
    fun decodeQrContents(qrContents: String): String {

        // We are removing the first 16 characters because it is just a place holder
        val actualQrContents = qrContents.substring(16, qrContents.length)

        val decodedMessage = Base64.decode(actualQrContents, Base64.DEFAULT)

        val decodedJson = String(decodedMessage)

        Timber.d("Decoded JSON -> $decodedJson")

        return decodedJson
    }

    private fun testAesEncryption(context: Context) {
        val aes = AES()

        val encryptedData = aes.encrypt(context = context, strToEncrypt = "Shrayan Bajracharya")
        Timber.d("Encrypted data -> $encryptedData")

        val decryptedData = aes.decrypt(context = context, dataToDecrypt = encryptedData)
        Timber.d("Decrypted data -> $decryptedData")
    }

    private fun testQrCodeDecryption() {
        try {
            val key = AppConstants.keyValue.toByteArray()
            val encryptedString =
                "WJD65flSVJdhCsjQKNpogfoVEMyM0QCbeDkxZ8aKwHq1cjXjxJ7b3TGfi1BgBJzjtXAV+D1ksn3y4YSq7k2Tdyi7Dm5ZtcZjFm2uhZ/ewCRFi+9lDC/RczWnbWiCfnYCT6GMD5Pjeeg/B8XmUMz49ODWK0uRETVt6p8VJML4aeMaXxpdMXUE5gLdsV+KHWJtX4tbT8LVlyUlyO1cjlEqDD0OW3bqiqPpcYEtfavNO0BX/rin9r7OomOTbXduum7tJs92dWZDjY6YWNmlLcub30RWE1Z98hGkqf6bnYMFt/PejxzG1LPeW9wygk3S8qVoh+EgIviR8lypLs3yymW6xBNFNR+wONE6OziloN5lyTrl8lso00K94kgVCVD2+kLxHXiMIwDgBxPaWtvu4G5yEw=="

            val decryptedString = AES().decryptString(encryptedString, key)
            println("Decrypted message: $decryptedString")
        } catch (e: Exception) {
            Timber.d("Caught exception while trying to decrypt -> ${e.message}")
        }
    }


}