package com.kash4me.data.models.merchant.cashback_code


import com.google.gson.annotations.SerializedName

data class SendCashbackCodeResponse(
    @SerializedName("amount")
    val amount: String? = "",
    @SerializedName("token")
    val token: String? = ""
)