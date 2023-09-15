package com.kash4me.data.models.merchant.calculate_cashback


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class CalculateCashbackRequest(
    @SerializedName("amount_spent")
    val amountSpent: String? = "",
    @SerializedName("cashback_settings")
    val cashbackSettings: Int? = 0,
    @SerializedName("customer")
    val customer: Int? = 0,
    @SerializedName("shop_id")
    val shopId: Int? = 0
)