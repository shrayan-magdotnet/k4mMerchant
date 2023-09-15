package com.kash4me.data.models.customer.response


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class CustomerTransactionsResponseV2(
    @SerializedName("count")
    val count: Int? = 0,
    @SerializedName("next")
    val next: String? = "",
    @SerializedName("previous")
    val previous: String? = "",
    @SerializedName("results")
    val results: List<Result?>? = listOf(),
    @SerializedName("transaction_details")
    val transactionDetails: TransactionDetails? = TransactionDetails()
) {
    @Keep
    data class Result(
        @SerializedName("amount")
        val amount: String? = "",
        @SerializedName("after_amount")
        val afterAmount: String? = "",
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
            @SerializedName("cashback_amount")
            val cashbackAmount: Double? = 0.0,
            @SerializedName("fee_amount")
            val feeAmount: Double? = 0.0
        )
    }

    @Keep
    data class TransactionDetails(
        @SerializedName("cashback_balance")
        val cashbackBalance: String? = "",
        @SerializedName("processing")
        val processing: String? = ""
    )
}