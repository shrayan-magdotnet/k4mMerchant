package com.kash4me.data.models.merchant.cashback


import com.google.gson.annotations.SerializedName

data class AddCashbackRequest(
    @SerializedName("cashback_amount")
    var cashbackAmount: String? = "",
    @SerializedName("cashback_type")
    var cashbackType: Int? = 0,
    @SerializedName("maturity_amount")
    var maturityAmount: String? = ""
)