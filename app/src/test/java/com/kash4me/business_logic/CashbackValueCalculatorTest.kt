package com.kash4me.business_logic

import com.kash4me.data.models.merchant.purchase_return.ReturnPurchaseQr
import org.junit.Test

class CashbackValueCalculatorTest {

    @Test
    fun calculateCashback() {

        val purchaseAmount = 1000.00
        val cashbackSettings = ReturnPurchaseQr.ActiveCashbackSettings(
            cashbackAmount = "125.00",
            cashbackPercentage = "5.00",
            cashbackType = 1,
            id = 92,
            maturityAmount = "2500.00"

        )
        val cashbackValue = CashbackValueCalculator().getCashbackValue(
            purchaseAmount = purchaseAmount,
            activeCashbackSettings = cashbackSettings
        )
        println("Cashback value -> $cashbackValue")

    }

}