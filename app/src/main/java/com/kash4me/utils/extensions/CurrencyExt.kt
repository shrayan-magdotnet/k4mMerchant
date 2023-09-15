package com.kash4me.utils.extensions

import com.blackcat.currencyedittext.CurrencyEditText
import timber.log.Timber
import java.text.DecimalFormat

fun Double.formatUsingNepaliCurrencySystem(): String {

    val roundedOffAmount = this.roundOffToTwoDecimalDigits()

    val formatter = DecimalFormat("##,##,##,##0.00")
    return formatter.format(roundedOffAmount)

}

fun Double.formatUsingCurrencySystem(): String {

    val roundedOffAmount = this.roundOffToTwoDecimalDigits()

    val formatter = DecimalFormat("###,###,##0.00")
    return formatter.format(roundedOffAmount)

}

fun CurrencyEditText.getAmount(): Double {

    return this.rawValue.convertRawValueToActualAmount()

}

fun Long.formatToTwoDigit(): Double {
    val result = this / 100
    Timber.d("Result -> $result")
    val decimal = this % 100
    Timber.d("Remainder -> $decimal")
    val actual = result + (decimal.toDouble() * 0.01)
    Timber.d("Actual -> $actual")
    return actual
}

/**
 * This function is primary used for converting raw value to actual amount of [CurrencyEditText]
 */
fun Long.convertRawValueToActualAmount(): Double {
    val inputInDouble = this.toDouble() / 100.00
    return inputInDouble.roundOffToTwoDecimalDigits()
}

fun String.getAmountInDouble(): Double {

    val amountInString: String

    val amountInDouble: Double = try {
        this.toDouble()
    } catch (ex: NumberFormatException) {
        amountInString = this.replace(",", "")
        amountInString.toDouble()
    }

    return amountInDouble

}