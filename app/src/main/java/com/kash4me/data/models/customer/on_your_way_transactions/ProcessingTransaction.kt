package com.kash4me.data.models.customer.on_your_way_transactions


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ProcessingTransaction(
    @SerializedName("created_at")
    val createdAt: String? = "",
    @SerializedName("params")
    val params: Params? = Params(),
    @SerializedName("shop_name")
    val shopName: String? = "",
    @SerializedName("transaction_type")
    val transactionType: String? = ""
) {
    @Keep
    data class Params(
        @SerializedName("amount_spent")
        val amountSpent: String? = "",
        @SerializedName("cashback_amount")
        val cashbackAmount: String? = ""
    )
}