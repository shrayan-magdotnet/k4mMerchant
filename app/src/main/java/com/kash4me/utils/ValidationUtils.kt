package com.kash4me.utils

import java.net.URL

class ValidationUtils {

    fun isValidUrl(urlString: String): Boolean {
        return try {
            val url = URL(urlString)
            // If the URL was successfully parsed, it's valid
            true
        } catch (e: Exception) {
            // Malformed URL or other exception occurred, so it's not valid
            false
        }
    }

}