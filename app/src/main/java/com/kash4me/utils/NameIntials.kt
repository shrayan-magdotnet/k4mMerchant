package com.kash4me.utils

fun getNameInitials(name: String): String {
    try {
        if (name.isNotEmpty()) {
            return getFirstAndLastLetters(input = name)
        }
        return ""
    } catch (ex: Exception) {
        return ""
    }
}

fun getFirstLetters(input: String): String {
    // Split the input string into individual words
    val words = input.split(" ")
    // Extract the first character of each word and convert it to uppercase
    val firstLetters = words.map { it.first().uppercase() }
    // Concatenate the first characters to form the final result
    return firstLetters.joinToString("")
}

fun getFirstAndLastLetters(input: String): String {
    val words = input.trim().split("\\s+".toRegex()) // Split sentence into words
    val firstLetter = words.firstOrNull()?.get(0) ?: ' ' // Get first letter of first word
    val lastLetter = words.lastOrNull()?.get(0) ?: ' '   // Get first letter of last word
    return firstLetter.uppercase() + lastLetter.uppercase()
}
