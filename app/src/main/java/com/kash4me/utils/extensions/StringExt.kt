package com.kash4me.utils.extensions

fun String?.getEmptyIfNull() = this ?: ""

fun String?.getNotAvailableIfEmptyOrNull(): String {
    return if (this.isNullOrBlank()) {
        "N/A"
    } else {
        this
    }
}

fun String.capWords(): String {

    return try {
        this.split(" ")
            .joinToString(" ") { it.replace(it.get(0), it.get(0).uppercaseChar()) }
    } catch (ex: StringIndexOutOfBoundsException) {
        ""
    }

}

fun String.formatAsCurrency(): String {

    return this.toDoubleOrNull()?.formatUsingCurrencySystem() ?: "0.00"

}