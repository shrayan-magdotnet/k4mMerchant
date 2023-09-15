package com.kash4me.data.models.merchant.purchase_return


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ReturnPurchaseRequest(
    @SerializedName("transaction_id")
    val transactionId: Int? = 0,
    @SerializedName("amount_spent")
    val amountSpent: String? = "",
    @SerializedName("qr_token")
    val qrToken: String? = ""
)