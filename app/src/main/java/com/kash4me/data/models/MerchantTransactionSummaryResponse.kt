package com.kash4me.data.models

import com.google.gson.annotations.SerializedName

data class MerchantTransactionSummaryResponse(
    val monthly: Monthly?,
    val today: Today?,
    val weekly: Weekly?,
    @SerializedName("payment_status")
    val isPaymentSetupComplete: Boolean? = null
)

data class Monthly(
    val count: Int?,
    val total_amount: String?,
    val total_cashback: String?
)

data class Today(
    val count: Int?,
    val total_amount: String?,
    val total_cashback: String?
)

data class Weekly(
    val count: Int?,
    val total_amount: String?,
    val total_cashback: String?
)