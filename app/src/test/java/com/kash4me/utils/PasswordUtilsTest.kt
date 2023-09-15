package com.kash4me.utils

import com.google.common.truth.Truth
import org.junit.Test

class PasswordUtilsTest {

    @Test
    fun hasNoUppercase_hasNoSpecialCharacters_hasNoNumbers_returnsFalse() {

        val input = "adminadmin"
        println("Input -> $input")
        val isPasswordValid = PasswordUtils().isPasswordValid(input)
        println("isPasswordValid -> $isPasswordValid")
        Truth.assertThat(isPasswordValid).isFalse()

    }

    @Test
    fun hasUppercase_hasNoSpecialCharacters_hasNoNumbers_returnsFalse() {

        val input = "Adminadmin"
        println("Input -> $input")
        val isPasswordValid = PasswordUtils().isPasswordValid(input)
        println("isPasswordValid -> $isPasswordValid")
        Truth.assertThat(isPasswordValid).isFalse()

    }

    @Test
    fun hasUppercase_hasSpecialCharacters_hasNoNumbers_returnsFalse() {

        val input = "Admin@admin"
        println("Input -> $input")
        val isPasswordValid = PasswordUtils().isPasswordValid(input)
        println("isPasswordValid -> $isPasswordValid")
        Truth.assertThat(isPasswordValid).isFalse()

    }

    @Test
    fun hasUppercase_hasSpecialCharacters_hasNumbers_returnsTrue() {

        val input = "Admin@123"
        println("Input -> $input")
        val isPasswordValid = PasswordUtils().isPasswordValid(input)
        println("isPasswordValid -> $isPasswordValid")
        Truth.assertThat(isPasswordValid).isTrue()

    }

}