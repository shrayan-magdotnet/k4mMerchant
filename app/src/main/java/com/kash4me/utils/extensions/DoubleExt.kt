package com.kash4me.utils.extensions

fun Double?.getZeroIfNull() = this ?: 0.0

fun Double?.toLong(): Long? {
    return this?.times(100L)?.toLong()
}

fun Double.roundOffToTwoDecimalDigits(): Double {
    return try {
        val number3digits: Double = String.format("%.3f", this).toDouble()
        String.format("%.2f", number3digits).toDouble()
    } catch (ex: Exception) {
        this
    }
}

fun Double.roundOffToThreeDecimalDigits(): Double {
    return String.format("%.3f", this).toDouble()
}

fun Double.roundOffToDecimalDigits(n: Int): Double {
    return String.format("%.${n}f", this).toDouble()
}