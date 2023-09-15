package com.kash4me.utils

class PasswordUtils {

    fun isPasswordValid(input: String): Boolean {
        val digitRegex = ".*\\d.*" // Matches any digit
        val specialCharRegex = ".*[^a-zA-Z0-9].*" // Matches any non-alphanumeric character
        val capitalLetterRegex = ".*[A-Z].*" // Matches any capital letter

        val containsDigit = input.matches(digitRegex.toRegex())
        val containsSpecialChar = input.matches(specialCharRegex.toRegex())
        val containsCapitalLetter = input.matches(capitalLetterRegex.toRegex())
        val meetsMinimumPasswordLength = input.length >= AppConstants.MIN_PASSWORD_LENGTH

        return meetsMinimumPasswordLength && containsDigit && containsSpecialChar && containsCapitalLetter
    }


}