package com.kash4me.data.models.merchant.rollback_transaction


import com.google.gson.annotations.SerializedName

data class RollbackTransactionResponse(
    @SerializedName("message")
    val message: String? = ""
)