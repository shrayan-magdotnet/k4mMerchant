package com.kash4me.data.models.merchant.cashback_code


import com.google.gson.annotations.SerializedName

data class SendCashbackCodeRequest(
    @SerializedName("amount")
    val amount: String? = "",
    @SerializedName("email_address")
    val emailAddress: String? = ""
)