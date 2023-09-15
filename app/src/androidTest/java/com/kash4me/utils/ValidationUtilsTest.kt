package com.kash4me.utils

import com.google.common.truth.Truth

import org.junit.Test

class ValidationUtilsTest {

    @Test
    fun validUrl_returnsTrue() {

        val url = "https://www.google.com"

        val isValidUrl = ValidationUtils().isValidUrl(url)
        println("URL: $url")
        println("is Valid URL (Expected) -> true")
        println("is Valid URL (Actual) -> $isValidUrl")

        Truth.assertThat(isValidUrl).isTrue()
    }

    @Test
    fun invalidUrl_returnsFalse() {

        val url = "hey_yo"

        val isValidUrl = ValidationUtils().isValidUrl(url)

        println("URL: $url")
        println("is Valid URL (Expected) -> false")
        println("is Valid URL (Actual) -> $isValidUrl")

        Truth.assertThat(isValidUrl).isFalse()

    }
}