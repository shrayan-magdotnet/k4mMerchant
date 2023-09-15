package com.kash4me.data.models.merchant.accept_kash4me_payment


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AcceptKash4mePaymentRequest(
    @SerializedName("customer")
    val customer: Int? = 0,
    @SerializedName("customer_transaction")
    val customerTransaction: CustomerTransaction? = CustomerTransaction(),
    @SerializedName("purchase_amount")
    val purchaseAmount: String? = "",
    @SerializedName("qr_token")
    val qrToken: String? = ""
) {
    @Keep
    data class CustomerTransaction(
        @SerializedName("cashback_amount")
        val cashbackAmount: String? = "",
        @SerializedName("transaction_fee")
        val transactionFee: String? = "",
        @SerializedName("withdraw_amount")
        val withdrawAmount: String? = ""
    )
}