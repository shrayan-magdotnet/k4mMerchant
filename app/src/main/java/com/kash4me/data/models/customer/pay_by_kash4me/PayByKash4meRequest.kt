package com.kash4me.data.models.customer.pay_by_kash4me


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class PayByKash4meRequest(
    @SerializedName("withdraw_amount")
    val withdrawAmount: String? = ""
)