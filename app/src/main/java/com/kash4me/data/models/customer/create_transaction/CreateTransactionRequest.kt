package com.kash4me.data.models.customer.create_transaction


import com.google.gson.annotations.SerializedName

data class CreateTransactionRequest(
    @SerializedName("transaction_amount")
    val transactionAmount: String? = ""
)