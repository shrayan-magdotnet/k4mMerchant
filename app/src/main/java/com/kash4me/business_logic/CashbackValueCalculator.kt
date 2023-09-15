package com.kash4me.business_logic

import com.kash4me.data.models.merchant.purchase_return.ReturnPurchaseQr
import com.kash4me.utils.extensions.getZeroIfNull

class CashbackValueCalculator {

    fun getCashbackValue(
        purchaseAmount: Double,
        activeCashbackSettings: ReturnPurchaseQr.ActiveCashbackSettings?
    ): Double? {

        if (activeCashbackSettings == null) {
            return null
        }

        val percentage = activeCashbackSettings.cashbackPercentage?.toDoubleOrNull()
        return purchaseAmount * (percentage?.div(100.0).getZeroIfNull())

    }

}