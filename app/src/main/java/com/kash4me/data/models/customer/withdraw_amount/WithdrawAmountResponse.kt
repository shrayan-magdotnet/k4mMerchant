package com.kash4me.data.models.customer.withdraw_amount


import com.google.gson.annotations.SerializedName

data class WithdrawAmountResponse(
    @SerializedName("answer")
    val answer: String? = "",
    @SerializedName("payment_type")
    val paymentType: String? = "",
    @SerializedName("question")
    val question: String? = "",
    @SerializedName("transaction_amount")
    val transactionAmount: String? = ""
)