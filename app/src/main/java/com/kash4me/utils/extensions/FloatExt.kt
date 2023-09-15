package com.kash4me.utils.extensions

fun Float.toDoubleOrNull(): Double? {
    return try {
        this.toDouble()
    } catch (ex: Exception) {
        null
    }
}