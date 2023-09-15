package com.kash4me.data.models.customer.transactions_according_to_merchant

import com.google.gson.annotations.SerializedName

data class TransactionsAccordingToMerchantResponse(
    val count: Int,
    val next: String,
    val previous: String,
    val results: List<Result>,
    @SerializedName("cashback_balance")
    val cashbackBalance: String
)