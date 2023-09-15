package com.kash4me.utils.extensions

import org.junit.Test

class CurrencyExtKtTest {

    @Test
    fun convertRawValueToActualAmount() {

        val input: Long = 123456789
        val output = input.convertRawValueToActualAmount()
        println("Input: $input Output: $output")

    }

    @Test
    fun invalidAmount_throwsException() {

        val amountInString = "8,000,000"
        val amount = amountInString.getAmountInDouble()

        println("Amount in double: " + amount)

    }


}