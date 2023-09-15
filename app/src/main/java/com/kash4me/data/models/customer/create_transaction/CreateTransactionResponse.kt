package com.kash4me.data.models.customer.create_transaction


import com.google.gson.annotations.SerializedName

data class CreateTransactionResponse(
    @SerializedName("transaction_amount")
    val transactionAmount: String? = "",
    @SerializedName("balance_amount")
    val balanceAmount: String? = "",
    @SerializedName("created_at")
    val createdAt: String? = "",
    @SerializedName("transaction_type")
    val transactionType: String? = ""
)