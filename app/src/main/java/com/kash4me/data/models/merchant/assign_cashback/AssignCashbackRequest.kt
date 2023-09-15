package com.kash4me.data.models.merchant.assign_cashback


import com.google.gson.annotations.SerializedName

data class AssignCashbackRequest(
    @SerializedName("amount_spent")
    val amountSpent: String? = "",
    @SerializedName("cashback_settings")
    val cashbackSettings: Int? = 0,
    @SerializedName("customer")
    val customer: Int? = 0,
    @SerializedName("shop_id")
    val shopId: Int? = 0
)