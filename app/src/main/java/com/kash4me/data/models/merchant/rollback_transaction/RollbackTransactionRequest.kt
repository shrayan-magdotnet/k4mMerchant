package com.kash4me.data.models.merchant.rollback_transaction


import com.google.gson.annotations.SerializedName

data class RollbackTransactionRequest(
    @SerializedName("amount_spent")
    val amountSpent: String? = "",
    @SerializedName("customer")
    val customerId: Int? = 0,
    @SerializedName("merchant")
    val merchantId: Int? = 0
)