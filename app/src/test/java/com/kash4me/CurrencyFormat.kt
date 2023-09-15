package com.kash4me

import com.kash4me.utils.extensions.formatUsingNepaliCurrencySystem
import org.junit.Test

class CurrencyFormat {

    @Test
    fun format() {

        val input = "123456789"
        val output = input.toDoubleOrNull()?.formatUsingNepaliCurrencySystem()
        println("Output -> $output")

    }

}