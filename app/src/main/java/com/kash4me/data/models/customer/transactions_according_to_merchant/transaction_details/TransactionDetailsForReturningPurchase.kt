package com.kash4me.data.models.customer.transactions_according_to_merchant.transaction_details

import com.google.gson.annotations.SerializedName

data class TransactionDetailsForReturningPurchase(
    @SerializedName("allow_rollback")
    val allowRollback: Boolean? = null,
    val amount_spent: String?,
    val cashback_amount: String?,
    val created_at: String?,
    val id: Int?,
    val merchant_name: String?,
    val qr_image: String?,
    @SerializedName("timezone")
    val timezone: String?,
)